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
    viewModel(qualifier<T>()) { params -> StoreViewModel(get<T> { params }) }
}

@Composable
inline fun <reified T : Container<S, I, A>, S : MVIState, I : MVIIntent, A : MVIAction> storeViewModel(
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
    key: String? = null,
    extras: CreationExtras = defaultExtras(viewModelStoreOwner),
    scope: Scope = getKoinScope(),
    noinline parameters: ParametersDefinition? = null,
): StoreViewModel<S, I, A> = getViewModel(qualifier<T>(), viewModelStoreOwner, key, extras, scope, parameters)

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

It doesn't matter which UI framework you use. Neither your Contract or your Container/ViewModel will change in any way.

### Compose

!> Compose does not play well with MVVM+ style because of the instability of the `LambdaIntent` and `ViewModel` classes.
It is discouraged to use Lambda intents with Compose as that will not only leak the context of the store but
also degrade performance. The Compose DSL of the library does not support MVVM+ because of this.

You don't have to annotate your state with `@Immutable` as `MVIState` is already marked immutable.

```kotlin
@Composable
internal fun CounterScreen() {
    // using Koin DSL from above
    val store = storeViewModel<CounterContainer, _, _, _>()

    val state by store.subscribe { action ->
        when (action) {
            is ShowMessage -> {
                /* ... */
            }
        }
    }
    CounterScreenContent(store, state)
}

@Composable
private fun IntentReceiver<CounterIntent>.CounterScreenContent(state: CounterState) {
    when (state) {
        is DisplayingCounter -> {
            Button(onClick = { intent(ClickedCounter) }) { // intent() available from the receiver parameter
                Text("Counter: ${state.counter}")
            }
        }
        /* ... */
    }
}
```

Under the hood, the `subscribe` function will efficiently subscribe to the store (it is lifecycle-aware) and
use the composition scope to process your events. Event processing will stop in `onPause()` of the parent activity.
In `onResume()`, the composable will resubscribe. Your composable will recompose when state changes, but not
resubscribe to events. The lifecycle state is customizable.

?> Compose plays well with MVI style because state changes will trigger recompositions. Just mutate your state,
and the UI will update to reflect changes. Also, the `IntentReceiver` is `@Stable`, so you can safely send intents from
anywhere without awkward method references and unstable lambdas.

* Use the lambda parameter of `subscribe` to subscribe to `MVIActions`.
  Those will be processed as they arrive and the `consume` lambda
  will **suspend** until an action is processed. Use a receiver coroutine scope to
  launch new coroutines that will parallelize your flow (e.g. for snackbars).
* A best practice is to make your state handling (UI redraw composable) a pure function and extract it to a separate
  Composable such as `ScreenContent(state: ScreenState)` to keep your `*Screen` function clean, as shown above.
* If you want to send `MVIIntent`s from a nested composable, just use `IntentReceiver` as a context.

If you have defined your `*Content` function, you will get a composable that can be easily used in previews.
That composable will not need DI, Local Providers from compose, or anything else for that matter, to draw itself.
But there's a catch: It has an `IntentReceiver<I>` as a parameter. To deal with this, there is an `EmptyReceiver`
composable. EmptyReceiver does nothing when an intent is sent, which
is exactly what we want for previews. We can now define our `PreviewParameterProvider` and the Preview composable.

```kotlin
private class PreviewProvider : StateProvider<CounterState>(
    DisplayingCounter(1, 2, "param"),
    Loading,
)

@Composable
@Preview
private fun CounterScreenPreview(
    @PreviewParameter(PreviewProvider::class) state: CounterState,
) = EmptyReceiver {
    ComposeScreenContent(state)
}
```

See
the [Sample app](https://github.com/respawn-app/FlowMVI/blob/master/app/src/main/kotlin/pro/respawn/flowmvi/sample/compose/ComposeScreen.kt)
for a more elaborate example.

## View

For a View-based project, the logic is essentially the same.

* Subscribe in `Fragment.onViewCreated` or `Activity.onCreate`. The library will handle the lifecycle for you.
* Make sure your `render` function is pure, and `consume` function does not loop itself with intents.
* Always update **all views** in `render`, for **any state change**, to circumvent the problems of old-school stateful
  view-based Android API.

```kotlin
class CounterFragment : Fragment() {

    private val binding by viewBinding<CounterFragmentBinding>()
    private val store by storeViewModel<CounterContainer, _, _, _>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribe(container, ::consume, ::render)

        with(binding) {
            tvCounter.setOnClickListener(store::onClickCounter) // let's say we are using MVVM+ style.
        }
    }

    private fun render(state: ScreenState): Unit = with(binding) {
        with(state) {
            tvCounter.text = counter.toString()
            /* ... update ALL views! ... */
        }
    }

    private fun consume(action: ScreenAction): Unit = when (action) {
        is ShowMessage -> Snackbar.make(binding.root, action.message, Snackbar.LENGTH_SHORT).show()
    }
}
```

See the [Sample app](https://github.com/respawn-app/FlowMVI/blob/master/app/src/main/kotlin/pro/respawn/flowmvi/sample/view/BasicActivity.kt)
for a more elaborate example.
