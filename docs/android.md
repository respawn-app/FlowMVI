Learn how to use FlowMVI with Android.
Start off by describing your Contract as outlined in the [Guide](usage.md) section.

### ViewModel

```kotlin
internal class ScreenViewModel : MVIViewModel<ScreenState, ScreenIntent, ScreenAction>(initialState = Loading) {

    override fun recover(from: Exception) = Error(from) // optional

    override suspend fun reduce(intent: ScreenIntent) {
        when (intent) {
            //no-op if state is not DisplayingCounter
            is ClickedCounter -> updateState<DisplayingCounter> { //this -> DisplayingCounter

                ShowMessage("Incremented counter").send()

                copy(counter = counter + 1)
            }
            /* ... */
        }
    }
}
```

1. The `MVIViewModel` is just a huge `reduce` function. It can have its own state, but try to avoid creating a bunch of
   pieces of state that are not slaves to the `state` property, or you risk introducing inconsistencies to your business
   logic
2. Avoid exposing **any** public functions or properties of the `ViewModel`
   (literally everything should ideally be private). Use the `MVIProvider` interface and that's
   it. By following the contract, you will ensure there are no unintended side effects to your logic.
3. The `reduce` function works the same way as the `reduce` lambda from Quickstart.
4. Use the `launchRecovering` helper function to launch coroutines from `reduce` or some other place.
5. The `recover` function will be called in case `launchRecovering` or `reduce` throws.
    * By default, `recover` just throws the exception.
    * Event processing will not be stopped when `recover` is invoked.
    * This drastically reduces crashes of the app due to developer errors, but recovering is optional.

It doesn't matter which UI framework you use. Neither your Contract or your ViewModel will change in any way.

## Compose

Don't forget to annotate your state with `@Immutable`. This will improve performance significantly.

```kotlin
@Immutable
sealed interface ScreenState {
    /* ... */
}
```

```kotlin
@Composable
internal fun CounterScreen() = MVIComposable(
    provider = getViewModel<ScreenViewModel>(),
) { state -> // this -> ConsumerScope

    consume { action ->
        when (action) {
            is ShowMessage -> {
                /* ... */
            }
        }
    }

    when (state) {
        is DisplayingCounter -> {
            Button(onClick = { send(ClickedCounter) }) {
                Text("Counter: ${state.counter}")
            }
        }
    }
}
```

Compose plays wonderfully with FlowMVI because state changes will trigger recompositions. Just mutate your state,
and the UI will update to reflect changes.

* A best practice is to make your state handling (UI redraw composable) a pure function and extract it to a separate
  Composable such as `ScreenContent(state: ScreenState)` to keep your `*Screen` function clean.
* Use the `consume` block to subscribe to `MVIActions`. Those will be processed as they arrive (no guarantees on their
  order) and the `consume` lambda will **suspend** until an action is processed. Use a receiver coroutine scope to
  launch new coroutines that will parallelize your flow.
* If you want to send `MVIIntent`s from a nested composable, just use `ConsumerScope` as a context:
  `ConsumerScope<ScreenIntent, ScreenAction>.ScreenContent(state: ScreenState)`. Use context receivers to clean up the
  declaration of the function.

Example of launching a nested coroutine:

```kotlin
fun CoroutineScope.snackbar(
    context: Context,
    text: String,
    snackbarState: SnackbarHostState,
    duration: SnackbarDuration = SnackbarDuration.Short,
) = launch {
    snackbarState.showSnackbar(text.string(context), duration = duration)
}

/* At the top of your MVIComposable... */

val context = LocalContext.current
val snackbarState = rememberSnackbarHostState()

consume { action ->
    when (action) {
        is ShowMessage -> snackbar(context, action.message, snackbarState) // does not block action processing
    }
}
```

Under the hood, the `MVIComposable` function will efficiently subscribe to the `provider` (it is lifecycle-aware) and
use the composition scope to process your events. Event processing will stop in `onPause()` of the parent activity.
In `onResume()`, the composable will resubscribe. MVIComposable will recompose when state changes, but not
resubscribe to events.

See [Sample app](https://github.com/respawn-app/FlowMVI/blob/master/app/src/main/kotlin/pro/respawn/flowmvi/sample/compose/ComposeScreen.kt)
for a more elaborate example.

## View

For a View-based project, inheritance rules. Just implement `MVIView` in your Fragment and subscribe to events.

```kotlin
internal class ScreenFragment : Fragment(), MVIView<ScreenState, ScreenIntent, ScreenAction> {

    private val binding by viewBinding<ScreenFragmentBinding>()

    override val provider by viewModel<ScreenViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribe() // one-liner for store subscription. Lifecycle-aware and efficient.

        with(binding) {
            tvCounter.setOnClickListener { send(ClickedCounter) }
        }
    }

    override fun render(state: ScreenState) = with(binding) {
        with(state) {
            tvCounter.text = counter.toString()
        }
    }

    override fun consume(action: ScreenAction) = when (action) {
        is ShowMessage -> Snackbar.make(binding.root, action.message, Snackbar.LENGTH_SHORT).show()
    }
}
```

1. Subscribe in `Fragment.onViewCreated` or `Activity.onCreate`. The library will handle lifecycle by itself. You
   don't want to access your binding when it is null, do you?
2. Make sure your `render` function is pure, and `consume` function does not loop itself with intents.
3. Always update **all views** in `render` to circumvent the problems of old-school stateful view-based Android
   API.
4. You are not required to extend `MVIView` - you can use `subscribe` anyway, just provide
   the `ViewLifecycleOwner.lifecycleScope` or `Activity.lifecycleScope` as a scope.

See [Sample app](https://github.com/respawn-app/FlowMVI/blob/master/app/src/main/kotlin/pro/respawn/flowmvi/sample/view/BasicActivity.kt)
for a more elaborate example.
