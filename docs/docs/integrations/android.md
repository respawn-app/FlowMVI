---
sidebar_position: 2
sidebar_label: Android
---

# Use FlowMVI with Android

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

The upside of this approach is that it's easier to implement and use some navigation-specific features
like `savedState` (you can still use them for KMP though)
The downside is that you lose KMP compatibility. If you have plans to make your ViewModels multiplatform,
it is advised to use the delegated approach instead, which is only slightly more verbose.

:::

### Delegated ViewModels

A slightly more advanced approach would be to avoid subclassing ViewModels altogether and using `ContainerViewModel`
that delegates to the Store.

This is a more robust and multiplatform-friendly approach that is slightly more boilerplatish, but does not require you
to subclass ViewModels.

The only caveat is injecting your `Container` into an instance of `StoreViewModel`, and then injecting the
`StoreViewModel` correctly. The implementation varies based on which DI framework you will be using, with some examples
are provided in the [DI Guide](/integrations/di.md)

## View Integration

For a View-based project, subscribe in an appropriate lifecycle callback and create two functions to render states
and consume actions.

* Subscribe in `Fragment.onViewCreated` or `Activity.onCreate`. The library will handle the lifecycle for you.
* Make sure your `render` function is idempotent, and `consume` function does not loop itself with intents.
* Always update **all views** in `render`, for **any state change**, to circumvent the problems of old-school stateful
  view-based Android API.

```kotlin
class CounterFragment : Fragment() {

    private val binding by viewBinding<CounterFragmentBinding>()
    private val store: CounterContainer by container() // see DI guide for implementation

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
