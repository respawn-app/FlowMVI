Here's how the library works at a glance:

![](images/FlowMVI.jpg)

## Step 1: Add Dependencies

To start with the library, first declare the dependencies.

![Maven Central](https://img.shields.io/maven-central/v/pro.respawn.flowmvi/core?label=Maven%20Central)

```toml
[versions]
flowmvi = "< Badge above 👆🏻 >"

[dependencies]
flowmvi-core = { module = "pro.respawn.flowmvi:core", version.ref = "flowmvi" } # multiplatform
flowmvi-android = { module = "pro.respawn.flowmvi:android", version.ref = "flowmvi" } # common android
flowmvi-view = { module = "pro.respawn.flowmvi:android-view", version.ref = "flowmvi" } # view-based android
flowmvi-compose = { module = "pro.respawn.flowmvi:android-compose", version.ref = "flowmvi" }  # compose
```

The `core` artifact supports:

* JVM: [ Android ],
* Linux [ X64, Mingw64 ],
* iOS: [ X64, Arm64, macOS ],
* js: [ nodejs, browser ]

## Step 2: Describe your Contract

It's best to start with looking at the actual UI of what you want to build, some logic that is possible, and build
on that. Describing the contract makes building the logic easier because you have everything you need at the start.

```kotlin
internal sealed interface ScreenState : MVIState {
    data object Loading : ScreenState
    data class Error(e: Exception) : ScreenState
    data class DisplayingCounter(
        val counter: Int,
    ) : ScreenState
}

internal sealed interface ScreenIntent : MVIIntent {
    data object ClickedCounter : ScreenIntent

    @JvmInline
    value class ChangedValue(val value: Int) : ScreenIntent

    data class GrantedPermission(val granted: Boolean, val permission: String) : ScreenIntent
}

internal sealed interface ScreenAction : MVIAction {
    data class ShowMessage(val message: String) : ScreenAction
}
```

1. The `MVIState` is what should be displayed or used by the UI layer. Whenever the state changes,
   update **all** of your UI with the current properties of the state.
    * Do **not** make your state mutable. Because FlowMVI uses `StateFlow`s under the hood, your state changes
      **won't be reflected** if you mutate your state using `var`s or
      by using mutable properties such as `MutableList` s.
      Use `copy()` of the data classes to mutate your state instead. Even if you use `List`s as the value type,
      for example, make sure those are **new** lists and not just `MutableList`s that were upcasted.
2. The `MVIIntent` is an action that the user or the subscriber takes.
   For example clicks, permission grants and dialog button presses.
    * Intents are processed **sequentially** one by one.
3. The `MVIAction` is a one-off event that should happen in the UI or that the subscriber should handle.
    * Examples include Snackbars, Popup messages, Sounds and so on.
    * Do not confuse States with Actions! Actions are **one-off, "fire and forget" events**.
    * Actions are **sent sequentially** and processed **in parallel**.

## Step 3: Create your business logic

Create either an `MVIStore`, or subclass `MVIViewModel` if you're on Android.

```kotlin
private val store by launchedStore<ScreenState, ScreenIntent, ScreenAction>(
    scope = eventProcessingCoroutineScope,
    initial = ScreenState.Loading,
    behavior = ActionShareBehavior.Distribute(),
    reduce = { intent -> /*...*/ },
)
```

1. Provide a scope where you want the business logic (event processing) to be done.
    * This scope should either be same
      or outlive the scope of the consumer of the events.
    * The store will be launched on first access (lazy initialization).
2. Provide an initial state you want to start with. Usually, this is the `Loading` state as it's easy to create in
   constructors where coroutines or properties are not accessible yet.
3. Provide an `ActionShareBehavior`. Use `Distribute` if unsure and change if you encounter problems.
    * `Distribute` will fan-out `Action`s without repeating them if there are multiple subscribers.
    * `Share` will send `MVIAction`s to all active subscribers.
      It will not resend any actions to inactive ones once they subscribe.
4. Provide the `reduce` lambda.
    * `reduce` is the place where you take an `MVIIntent` parameter and turn it into an `MVIState` and a combination
      of `MVIAction`s. Usually looks like a huge `when` block.
    * Inside the `ReducerScope` you have access to a child `coroutineScope` to launch coroutines, current state and
      methods to send `Intent`s and `Action`s.

Here's an example of what a typical `Reducer` can look like:

```kotlin
when (intent) {
    is ClickedCounter -> updateState<DisplayingContent> { // this -> DisplayingContent

        launchRecovering {
            updateState {
                delay(1000) // no one will be able to change the state until updateState returns
                DisplayingContent(
                    counter = current + 1,
                    timer = timer ?: (this as? DisplayingContent)?.timer ?: 0
                )
            }
        }

        Loading // ^withState
    }

    is ClickedBack -> send(GoToBasicActivity)
}
```

1. Use `MVIAction.send()` or `send(MVIAction)` to send Actions
2. You can send Intents from the `reduce` block that will loop back and trigger another invocation of `reduce`.
    * This lets you easily delegate your logic to other blocks, but this is similar to a `goto`, so try to not overdo
      it.
3. When you want to change the state, use `updateState<DesiredStateType>()`.
    * The lambda of the `updateState` will only be invoked when current state is of type `CurrentStateType`.
    * State updates are **synchronous** and based on a first come - first serve basis. **All other coroutines that
      attempt to get current state will suspend until the previous one finishes**. This will ensure your state is
      updated atomically and thread-safely. Try to keep the computations in the `updateState` as lean as possible.
    * A good rule of thumb is to move everything that does not need access to the current state out of
      the `updateState` block.
4. When you want to get the current state or make your logic conditional based on state, but **don't want to change the
   state**, use `withState<ScreenState, _>()`.
    * The underscore is the inferred return type of the `withState` block.
    * `withState` otherwise functions similar to `updateState`

## Step 4: Subscribe to your Provider

### Core

```kotlin
store.subscribe(
    consumerCoroutineScope,
    consume = { action -> /* ... */ },
    render = { state -> /* ... */ },
)
```

1. Provide a consumer coroutine scope. This is the scope where the `consume` and `render` blocks will be run.
    * Subscribe to the store as many times as you want. If there were no other subscribers, your `Actions` will wait in
      a queue.
2. Handle one-off events that were sent in the `consume` block. You can send new intents from that block,
   but beware of infinite loops.
3. Update **all of your** UI in the `render` block.
    * This will make your UI just a simple slave to the `store` and ensure that all changes in the state are reflected
      properly. This is the beauty of MVI: No inconsistencies between what the business logic thinks and what is
      actually presented.
    * The state can change pretty frequently, so avoid any long computations in the block
    * The `render` block must be a [pure function](https://en.wikipedia.org/wiki/Pure_function), avoid sending Intents
      or Actions in that block. Send Intents from click listeners or other callbacks instead.

## All Done!

* If you're on android, see [Android guidelines](android.md) for info on how to use FlowMVI with Android.
* Full-fledged support for iOS is on its way but with a few tweaks for coroutines, you can use FlowMVI with iOS too.