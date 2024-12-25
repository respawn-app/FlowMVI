---
sidebar_position: 2
sidebar_label: Android
---

# Learn how to use FlowMVI with Android

There are multiple options on how to organize your code when working with Android.
The choice depends on your project's specific needs and each option has certain tradeoffs.

## ViewModel

### Direct ViewModels

The simplest way to organize code is to implement `Container` in your ViewModel.
You are not required to implement any interfaces, however. They are only served as markers/nice dsl providers.

Example that uses models from [quickstart](../quickstart.md) and MVVM+ style.
This example is also fully implemented in the sample app.

```kotlin
class CounterViewModel(
    repo: CounterRepository,
    handle: SavedStateHandle,
) : ViewModel(), ImmutableContainer<CounterState, CounterIntent, CounterAction> {

    // the store is lazy here, which is good for performance if you use other properties of the VM.
    // if you don't want a lazy store, use the regular store() function here
    override val store by lazyStore(
        initial = Loading,
        scope = viewModelScope,
    ) {
        configure {
            debuggable = BuildConfig.DEBUG
        }
        enableLogging()
        parcelizeState(handle)

        /* ... everything else ... */
        reduceLambdas() // <-- don't forget that lambdas must still be reduced
    }

    fun onClickCounter() = store.intent {
        action(ShowCounterIncrementedMessage)
        updateState<DisplayingCounter, _> {
            copy(counter = counter + 1)
        }
    }
}
```

Prefer to extend `ImmutableContainer` as that will hide the `intent` function from outside code, otherwise you'll leak
the `PipelineContext` of the store to subscribers.

:::info

The upside of this approach is that it's easier to implement and use some platform-specific features
like `savedState` (you can still use them for KMP though)
The downside is that you lose KMP compatibility. If you have plans to make your ViewModels multiplatform,
it is advised to use the delegated approach instead, which is only slightly more verbose.

:::

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
// declare
inline fun <reified T : Container<*, *, *>> Module.storeViewModel() {
    viewModel(qualifier<T>()) { params -> StoreViewModel(get<T> { params }) }
}

// inject
@Composable
inline fun <reified T : Container<S, I, A>, S : MVIState, I : MVIIntent, A : MVIAction> storeViewModel(
    noinline parameters: ParametersDefinition? = null,
): StoreViewModel<S, I, A> = getViewModel(qualifier = qualifier<T>(), parameters = parameters)

// use
val appModule = module {
    singleOf(::CounterRepository)
    factoryOf(::CounterContainer)
    storeViewModel<CounterContainer>()
}
```

:::warning[On type-safety]

Qualifiers are needed because you'll have many `StoreViewModels` that differ only by type of the container. Due to
type erasure, you must inject the VM by specifying a fully-qualified type
e.g. `StoreViewModel<CounterState, CounterIntent, CounterAction>`, or it will be replaced with `Store<*, *, *>` and the
DI framework will fail, likely in runtime.

:::

This is a more robust and multiplatform friendly approach that is slightly more boilerplatish but does not require you
to subclass ViewModels. This example is also demonstrated in the sample app.

## View Integration

For a View-based project, subscribe in an appropriate lifecycle callback and create two functions to render states
and consume actions.

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

    private fun render(state: CounterState): Unit = with(binding) {
        with(state) {
            tvCounter.text = counter.toString()
            /* ... update ALL views! ... */
        }
    }

    private fun consume(action: CounterAction): Unit = when (action) {
        is ShowMessage -> Snackbar.make(binding.root, action.message, Snackbar.LENGTH_SHORT).show()
    }
}
```
