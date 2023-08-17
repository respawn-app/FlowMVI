# Learn how to use FlowMVI with Android

There are multiple options on how to organize your code when working with Android.
The choice depends on your project's specific needs and each option has certain tradeoffs.

## ViewModel

### Direct ViewModels

The simplest way to organize code is to implement `Container` in your ViewModel.
You are not required to implement any interfaces, however. They are only served as markers/nice dsl providers.

Example that uses models from [quickstart](quickstart.md) and MVVM+ style.
This example is also fully implemented in the sample app.

```kotlin
class CounterViewModel(
    repo: CounterRepository,
    handle: SavedStateHandle,
) : ViewModel(), Container<CounterState, LambdaIntent<CounterState, CounterAction>, CounterAction> {

    // the store is lazy here, which is good for performance if you use other properties of the VM.
    // if you don't want a lazy store, use the regular store() function here
    override val store by lazyStore(
        initial = Loading,
        scope = viewModelScope,
    ) {
        // perks of direct approach
        debuggable = BuildConfig.DEBUG
        install(androidLoggingPlugin())
        parcelizeState(handle)

        /* ... everything else ... */
        reduceLambdas() // <-- don't forget that lambdas must still be reduced
    }

    fun onClickCounter() = store.intent {
        action(ShowSnackbar(R.string.lambda_processing))
        updateState<DisplayingCounter, _> {
            copy(counter = counter + 1)
        }
    }
}
```

?> The upside of this approach is that it's easier to implement and use some platform-specific features
like `savedState` (you can still use them for KMP though)
The downside is that you completely lose KMP compatibility. If you have plans to make your viewmodels multiplatform,
it is advised to use the delegated approach instead, which is only slightly more verbose.
This one can be the preferred approach, however, when migrating from FlowMVI 1.x as it's easier to handle.

### Delegated ViewModels

A slightly more advanced approach would be to avoid subclassing ViewModels altogether.

First, wrap your store in a simple class. You don't have to implement `Container` if you don't want to.

```kotlin
class CounterContainer(
    private val repo: CounterRepo,
) : Container<CounterState, CounterIntent, CounterAction> {
    
    override val store = store(Loading) {
        /* ... as before ... */
    }
}
```

From here, the only things left are to inject your `Container` into an instance of `StoreViewModel`, and then inject the
`StoreViewModel` correctly. The implementation varies based on which DI framework you will be using and how you want to
specify your qualifiers. The most basic setup for Koin will look like this:

```kotlin
inline fun <reified T : Container<*, *, *>> Module.storeViewModel() {
    viewModel(qualifier<T>()) { params -> StoreViewModel(get<T> { params }.store) }

    // using type qualifiers, can also use named() and companion objects
}

val appModule = module {
    singleOf(::CounterRepository)
    factoryOf(::CounterContainer)
    storeViewModel<CounterContainer>()
}
```

!> Qualifiers are needed because you'll have many `StoreViewModels` that differ only by type. Due to type erasure, you
must inject the VM by specifying a fully-qualified type
e.g. `StoreViewModel<CounterState, CounterIntent, CounterAction>`, or it will be replaced with `Store<*, *, *>` and the
DI framework will fail, likely in runtime.

This is a more robust and multiplatform friendly approach that is slightly more boilerplatish but does not require you
to subclass ViewModels.
The biggest downside of this approach is that you'll have to use qualifiers with your DI framework to distinguish
between different viewmodels. This example is also demonstrated in the sample app.

## UI Layer

It doesn't matter which UI framework you use. Neither your Contract or your ViewModel will change in any way.

### Compose

!> Compose does not play well with MVVM+ style because of the instability of the `LambdaIntent` and `ViewModel` classes.
It is highly discouraged to use Lambda intents with Compose as that will not only leak the context of the store but
also degrade performance. The Compose DSL of the library does not support MVVM+ because of this.

Don't forget to annotate your state with `@Immutable` if you can. This will improve performance significantly.

```kotlin
@Immutable
sealed interface CounterState: MVIState {
    /* ... */
}
```

Now you can define your composable:

```kotlin
private typealias Scope = ConsumerScope<CounterIntent, CounterAction>

@Composable
internal fun CounterScreen() = MVIComposable(
    // this doesn't look as cool as everything else, but you can write a dsl to make this a little better
    // see an example in the sample app
    getViewModel<StoreViewModel<CounterState, CounterIntent, CounterAction>>(qualifier<CounterContainer>()),
) { state -> // this -> Scope

    consume { action ->
        when (action) {
            is ShowMessage -> {
                /* ... */
            }
        }
    }
    CounterScreenContent(state)
}

@Composable
private fun Scope.CounterScreenContent(
    state: CounterState,
) {
    when (state) {
        is DisplayingCounter -> {
            Button(onClick = { intent(ClickedCounter) }) { // intent() available from scope
                Text("Counter: ${state.counter}")
            }
        }
        /* ... */
    }
}
```

Under the hood, the `MVIComposable` function will efficiently subscribe to the store (it is lifecycle-aware) and
use the composition scope to process your events. Event processing will stop in `onPause()` of the parent activity.
In `onResume()`, the composable will resubscribe. MVIComposable will recompose when state changes, but not
resubscribe to events. The lifecycle state is customizable.

?> Compose plays well with MVI style because state changes will trigger recompositions. Just mutate your state,
and the UI will update to reflect changes. Also, the `ConsumerScope` is `@Stable`, so you can safely send intents from
anywhere without awkward method references and unstable lambdas.

* Use the `consume` block to subscribe to `MVIActions`. Those will be processed as they arrive and the `consume` lambda
  will **suspend** until an action is processed. Use a receiver coroutine scope to
  launch new coroutines that will parallelize your flow (e.g. for snackbars).
* A best practice is to make your state handling (UI redraw composable) a pure function and extract it to a separate
  Composable such as `ScreenContent(state: ScreenState)` to keep your `*Screen` function clean, as shown above.
* If you want to send `MVIIntent`s from a nested composable, just use `ConsumerScope` as a context:
  `ConsumerScope<ScreenIntent, ScreenAction>.ScreenContent(state: ScreenState)`. Use type aliases to clean up the
  declaration of the function.

If you have defined your `*Content` function, you will get a composable that can be easily used in previews.
That composable will not need DI, Local Providers from compose, or anything else for that matter, to draw itself.
But there's a catch: It has a `ConsumerScope<I, A>` as a receiver. To deal with this, there is an `EmptyScope`
composable.
EmptyScope introduces a scope that does not collect actions and does nothing when an intent is sent, which is
exactly what we want for previews. We can now define our `PreviewParameterProvider` and the Preview composable.

```kotlin
private class PreviewProvider : StateProvider<CounterState>(
    DisplayingCounter(1, 2, "param"),
    Loading,
)

@Composable
@Preview
private fun CounterScreenPreview(
    @PreviewParameter(PreviewProvider::class) state: CounterState,
) = EmptyScope {
    ComposeScreenContent(state)
}
```

See [Sample app](https://github.com/respawn-app/FlowMVI/blob/master/app/src/main/kotlin/pro/respawn/flowmvi/sample/compose/ComposeScreen.kt)
for a more elaborate example.

## View

For a View-based project, inheritance rules. Just implement `MVIView` in your Fragment and subscribe to events.

```kotlin
class CounterFragment : Fragment(), MVIView<CounterState, CounterIntent, CounterAction> {
    
    private val binding by viewBinding<CounterFragmentBinding>()
    override val container by viewModel<CounterViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribe() // one-liner for store subscription. Lifecycle-aware and efficient.

        with(binding) {
            tvCounter.setOnClickListener(container::onClickCounter) // let's say we are using MVVM+ style.
        }
    }

    override fun render(state: ScreenState) = with(binding) {
        with(state) {
            tvCounter.text = counter.toString()
            /* ... update ALL views every time ... */
        }
    }

    override fun consume(action: ScreenAction) = when (action) {
        is ShowMessage -> Snackbar.make(binding.root, action.message, Snackbar.LENGTH_SHORT).show()
    }
}
```

* Subscribe in `Fragment.onViewCreated` or `Activity.onCreate`. The library will handle lifecycle for you.
* Make sure your `render` function is pure, and `consume` function does not loop itself with intents.
* Always update **all views** in `render`, for **any state change**, to circumvent the problems of old-school stateful
  view-based Android API.
* You are not required to extend `MVIView` - you can use `subscribe` anyway, just provide
  the `ViewLifecycleOwner.lifecycleScope` or `Activity.lifecycleScope` as a scope.

See [Sample app](https://github.com/respawn-app/FlowMVI/blob/master/app/src/main/kotlin/pro/respawn/flowmvi/sample/view/BasicActivity.kt)
for a more elaborate example.
