# Get Started with FlowMVI

In this guide, we'll build a feature with UI in 10 minutes.
First of all, here's how the library works:

* **Stores** are classes that _respond_ to **events**, called `Intent`s, and _update_ their **state**. Responding to
  Intents is called _reducing_.
* You _add functionality_ to Stores using `Plugin`s, which form a **pipeline**.
* Clients _subscribe_ to Stores to _render_ their **state** and _consume_ **side-effects**, called `Action`s.

![](images/chart.png)

## Step 1: Configure the library

### 1.1: Add dependencies

![Maven Central](https://img.shields.io/maven-central/v/pro.respawn.flowmvi/core?label=Maven%20Central)

<details>
<summary>Version catalogs</summary>

```toml
[versions]
flowmvi = "< Badge above 👆🏻 >"

[dependencies]
# Core KMP module
flowmvi-core = { module = "pro.respawn.flowmvi:core", version.ref = "flowmvi" }
# Test DSL
flowmvi-test = { module = "pro.respawn.flowmvi:test", version.ref = "flowmvi" }
# Compose multiplatform
flowmvi-compose = { module = "pro.respawn.flowmvi:compose", version.ref = "flowmvi" }
# Android (common + view-based)
flowmvi-android = { module = "pro.respawn.flowmvi:android", version.ref = "flowmvi" }
# Multiplatform state preservation
flowmvi-savedstate = { module = "pro.respawn.flowmvi:savedstate", version.ref = "flowmvi" }
# Remote debugging client
flowmvi-debugger = { module = "pro.respawn.flowmvi:debugger-plugin", version.ref = "flowmvi" }
# Essenty (Decompose) integration
flowmvi-essenty = { module = "pro.respawn.flowmvi:essenty", version.ref = "flowmvi" }
flowmvi-essenty-compose = { module = "pro.respawn.flowmvi:essenty-compose", version.ref = "flowmvi" } 
```

</details>

<details>
<summary>Gradle DSL</summary>

```kotlin
dependencies {
    val flowmvi = "< Badge above 👆🏻 >"
    // Core KMP module
    commonMainImplementation("pro.respawn.flowmvi:core:$flowmvi")
    // compose multiplatform
    commonMainImplementation("pro.respawn.flowmvi:compose:$flowmvi")
    // saving and restoring state
    commonMainImplementation("pro.respawn.flowmvi:savedstate:$flowmvi")
    // essenty integration
    commonMainImplementation("pro.respawn.flowmvi:essenty:$flowmvi")
    commonMainImplementation("pro.respawn.flowmvi:essenty-compose:$flowmvi")
    // testing DSL
    commonTestImplementation("pro.respawn.flowmvi:test:$flowmvi")
    // android integration
    androidMainImplementation("pro.respawn.flowmvi:android:$flowmvi")
    // remote debugging client (use on debug only)
    debugImplementation("pro.respawn.flowmvi:debugger-plugin:$flowmvi")
}
```

</details>

### 1.2 Configure JDK

<details>
<summary>Configure JDK</summary>

The library's minimum JVM target is set to 11 (sadly still not the default in Gradle).
If you encounter an error:

```
Cannot inline bytecode built with JVM target 11 into bytecode that
is being built with JVM target 1.8. Please specify proper '-jvm-target' option
```

Then configure your kotlin multiplatform compilation to target JVM 11 in your subproject's `build.gradle.kts`:

```kotlin
kotlin {
    androidTarget { // do the same for JVM/desktop target as well
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}


```

And in your android gradle files, set:
```kotlin
android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
```

If you support Android API <26, you will also need to
enable [desugaring](https://developer.android.com/studio/write/java8-support).

</details>

## Step 2: Choose your style

FlowMVI supports both MVI (strict model-driven logic) and the MVVM+ (functional, lambda-driven logic) styles.

* **Model-driven** means that you create an `MVIIntent` subclass for every event that happens, and the store
  decides how to handle it.
* **Functional** means that you invoke functions which contain your business logic, and then send the
  logic for processing to the store.

Model-driven intents are recommended to take full advantage of plugins and are explained below.

Functional intents look like this:

```kotlin
fun onItemClick(item: Item) = store.intent {
    updateState {
        copy(selectedItem = item)
    }
}
```

<details>
<summary>See this section if you can't decide</summary>

It's preferable to choose one style and use it throughout your project.
Each style has its own pros and cons, so choosing can be hard.
So please consider the following comparison:

### MVI style:

| Pros 👍                                                                                              | Cons 👎                                                                                                                         |
|------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| Greater separation of concerns as intent handling logic is strictly contained in the store's scope   | Boilerplatish: some intents will need to be duplicated for multiple screens, resulting in some amount of copy-paste             |
| Verbose and readable - easily understand which intent does what judging by the contract              | Hard to navigate in the IDE. You have to jump twice: first to the declaration, and then to usage, to see the code of the intent |
| Intents can be decomposed into sealed families, subclassed,  delegated, have properties or functions | Class explosion - class for every event may result in 50+ model classes per screen easily                                       |
| Intents can be re-sent inside the store - by sending an intent while handling another intent         | Sealed classes work worse for some platforms, for example, in Swift, Enums are not used and names are mangled                   |

### MVVM+ style:

| Pros 👍                                                                                                 | Cons 👎                                                                    |
|:--------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------|
| Elegant syntax - open a lambda block and write your logic there. Store's code remains clean             | You have to use `ImmutableStore` interface to not leak the store's context |
| Easily navigate to and see what an intent does in one click                                             | Lambdas are less performant than regular intents                           |
| Easier to support on other platforms if handled correctly (not exposing store's logic in platform code) | Some plugins will become useless, such as logging/time travel/analytics    |
| Get rid of all Intent classes entirely, avoid class explosion                                           | Intents cannot be composed, delegated and organized into families          |

* If you decide to use the MVVM+ style, consider using the `ImmutableStore` interface that won't let external code send
intents. This will prevent leaking the context of the store to subscribers.
* Additionally, you **must** install the `reduceLambdas` plugin to make the store handle your intents. Plugins are
  explained below.

</details>

## Step 3: Describe your Contract

A Contract consists of 3 parts:

* **States** that your Store can be in.
* **Intents** that the **Store** must _reduce_ (respond to)
* **Actions** that the **client** must __consume__ (take).

It looks like this:

```kotlin
// States
sealed interface CounterState : MVIState {
    data object Loading : CounterState
    data class Error(val e: Exception) : CounterState
    data class DisplayingCounter(val counter: Int) : CounterState
}

// MVI Style Intents
sealed interface CounterIntent : MVIIntent {

    data object ClickedNext : CounterIntent

    data class ChangedCounter(val value: Int) : CounterIntent

    data class GrantedPermission(val granted: Boolean, val permission: String) : CounterIntent
}

// MVVM+ Style Intents
typealias CounterIntent = LambdaIntent<CounterState, CounterAction>

// Side-effects
sealed interface CounterAction : MVIAction {

    data class ShowMessage(val message: String) : CounterAction
}
```

<details>
<summary>Click if you need help defining a contract</summary>

Describing the contract first makes building the logic easier because this helps make your business logic declarative.
To define your contract, ask yourself the following:

1. What can be shown at what times? Can the page be empty? Can it be loading? Can errors happen? -
   this will define your state family.
2. What elements can be shown on this screen, for each state? - these will define your state properties.
3. What can the user do on this screen? What can happen in the system? - these will be your Intents.
4. In response to given intents, what one-time events may happen? - these are Actions. Keep in mind that side-effects
   are not the best way to manage your business logic. Try to make your UI/platform logic stateful first, and resort
   to side-effects only if a third-party API you're working with is imperative (such as Android SDK or View system).

* The `MVIState` is what should be displayed or used by the UI layer. Whenever the state changes,
  update **all** of your UI with the current properties of the state.
    * Do **not** make your state mutable. Because FlowMVI uses `StateFlow`s under the hood, your state changes
      **won't be reflected** if you mutate your state using `var`s or
      by using mutable properties such as `MutableList`s.
      Use `copy()` of the data classes to mutate your state instead. Even if you use `List`s as the value type,
      for example, make sure those are **new** lists and not just `MutableList`s that were upcasted.
    * It's okay to copy the state often, modern devices can handle a few garbage collections.
* The `MVIIntent` is an action that the user or the subscriber takes, for example: clicks, system broadcasts and dialog
  button presses.
* The `MVIAction` is a one-off event that should happen in the UI or that the subscriber should handle.
    * Examples include snackbars, popup messages, sounds and so on.
  * Prefer using state instead of events if you are able to always know the
    outcome. [Read more here](https://proandroiddev.com/viewmodel-events-as-state-are-an-antipattern-35ff4fbc6fb6).
    * Do not confuse States with Actions! Actions are **one-off, "fire and forget" events** that cannot be tracked after being sent.
    * Actions are **sent and received sequentially**.
  * Intents are sent **to** the Store. Actions are sent **from** the Store.
    * Actions are not strictly guaranteed to be received by the subscriber, so do not use them for crucial elements of
      the logic.

</details>

* All Contract classes __must__ be **immutable** and **comparable**. If you don't define `equals`, your IDE will
  complain.
* Consider using the [IDE Plugin](https://plugins.jetbrains.com/plugin/25766-flowmvi) to generate the contract for you
  using the shortcut `fmvim`.
* If your store does not have a `State`, you can use an `EmptyState` object provided by the library.
* If your store does not have side effects, use `Nothing` in place of the side-effect type.

## Step 4: Configure your store

Here's a full list of things that can be done when configuring the store (with defaults assigned):

```kotlin
val store = store<CounterState, CounterIntent, CounterAction>(Loading) { // set initial state

    configure {
        debuggable = false
        name = null
        parallelIntents = false
        coroutineContext = EmptyCoroutineContext
        actionShareBehavior = ActionShareBehavior.Distribute()
        onOverflow = BufferOverflow.DROP_OLDEST
        intentCapacity = Channel.UNLIMITED
        atomicStateUpdates = true
        allowIdleSubscriptions = false
        logger = if (debuggable) PlatformStoreLogger else null
        verifyPlugins = debuggable
    }

    fun install(vararg plugins: LazyPlugin<S, I, A>)
    fun install(vararg decorators: StoreDecorator<S, I, A>)
    fun install(block: LazyPluginBuilder<S, I, A>.() -> Unit)
    fun decorate(decorator: DecoratorBuilder<S, I, A>.() -> Unit)
}
```

To get started, you only need to set the `debuggable` parameter - it enables a lot of additional
features like logging and validations.
Everything else has a decent default value - you can learn more in the section below.

<details>
<summary>See this section for full explanation of the properties</summary>

* `debuggable` - Setting this to `true` enables additional store validations and debug logging. The store will check
  your
  subscription events, launches/stops, and plugins for validity, as well as print logs to the system console.
* `name` - Set the future name of the store. Needed for debug, logging, comparing and injecting stores, analytics.
* `parallelIntents` - Declare that intents must be processed in parallel. Intents may still be dropped according to the
  `onOverflow` param.
* `coroutineContext` - A coroutine context override for the store. This context will be merged with the one the store
  was launched with (e.g. `viewModelScope`). All store operations will be launched in that context by default.
* `actionShareBehavior` - Define how the store handles and sends actions. Choose one of the following:
    * `Distribute` - send side effects in a fan-out FIFO fashion to one subscriber at a time (default).
  * `Share` - share side effects between subscribers using a `SharedFlow`. If an event is sent and there are no
    subscribers, the event **will be lost!**
    * `Restrict` - Allow only **one** subscription event per whole lifecycle of the store. If you want to subscribe
      again, you will have to re-create the store.
    * `Disable` - Disable side effects.
* `onOverflow` - Designate behavior for when the store's intent queue overflows. Choose from:
    * `BufferOverflow.SUSPEND` - Suspend on buffer overflow.
  * `BufferOverflow.DROP_OLDEST` - Drop **the oldest** value in the buffer on overflow, add the new value to the
    buffer, do not
      suspend (default).
  * `BufferOverflow.DROP_LATEST` - Drop **the latest** value that is being added to the buffer right now on buffer
    overflow (so that
      buffer contents stay the same), do not suspend.
* `intentCapacity` - Designate the maximum capacity of store's intent queue. This should be either:
    * A positive value of the buffer size
    * `Channel.UNLIMITED` - unlimited buffer (default)
    * `Channel.CONFLATED` - A buffer of 1
    * `Channel.RENDEZVOUS` - Zero buffer (all events not ready to be processed are dropped)
    * `Channel.BUFFERED` - Default system buffer capacity
* `atomicStateUpdates` - Enables transaction serialization for state updates, making state updates atomic and
  suspendable. Synchronizes state updates, allowing only **one** client to read and/or update the state at a time. All
  other clients that attempt to get the state will wait in a FIFO queue and suspend the parent coroutine. For one-time
  usage of non-atomic updates, see `updateStateImmediate`. Learn
  more [here](https://proandroiddev.com/how-to-safely-update-state-in-your-kotlin-apps-bf51ccebe2ef).
  Has a small performance impact because of coroutine context switching and mutex usage when enabled.
* `allowIdleSubscriptions` - A flag to indicate that clients may subscribe to this store even while it is not started.
  If you intend to stop and restart your store while the subscribers are present, set this to `true`. By default, will
  use the opposite value of the `debuggable` parameter (`true` on production).
* `logger` - An instance of `StoreLogger` to use for logging events. By default, the value is chosen based on
  the `debuggable` parameter:
    * `PlatformStoreLogger` that logs to the primary log stream of the system (e.g. Logcat on Android).
    * `NoOpStoreLogger` - if `debuggable` is false, logs will not be printed.

</details>

As it's super easy to reuse configurations, you may want to eventually setup [injection](plugins/debugging.md)
of the configuration.

Some interesting properties of the Store:

* Store can be `start`ed, `stop`ped, and restarted again as many times as you want. It will clean up everything except
  its state after itself.
* The store's subscribers will **not** wait until the store is started when they subscribe to the store.
  Such subscribers will not receive State updates or Actions. Don't forget to start the store.
* Stores are usually created eagerly, but the store *can* be lazy. There is `lazyStore()` for that.

## Step 5: Install plugins

FlowMVI is built entirely based on plugins!
**Everything** in FlowMVI is a plugin. This includes handling errors and even **reducing intents**.

For every store, you'll likely want to install a few plugins to add your business logic.
Prebuilt plugins come with a nice dsl when building a store. Check out the [plugins](plugins/prebuilt.md) page to learn
about all of them.

Call the `install` function using a prebuilt plugin, or use a lambda to create and install a plugin on the fly.

One plugin almost every store needs is the `reduce` plugin. Install it when building your store:

```kotlin
val counterStore = store<CounterState, CounterIntent, CounterAction>(Loading) {
    configure { /* ... */ }

    reduce { intent ->
        when (intent) {
            is ChangedCounter -> updateState<_, DisplayingCounter> {
                copy(intent.newValue)
            }
        }
    }
}
```

Every plugin has a special receiver called `PipelineContext`. It gives you access to everything you need:

* `updateState { }` - update the state of the store using the return value. Code in the block is thread-safe.
* `withState { }` - grab the state thread-safely and use it, but do not change it.
* `action()` - send a side-effect to subscribers
* `intent()` - re-send and delegate to another intent
* `config` - use the store's configuration, for example, to issue log calls:
  `config.logger.info { "counter = $counter" }` or just `log { "logs" }`. It will only print if `debuggable` is true.

## Step 6: Inject and provide dependencies

You'll likely want to:

1. Provide some dependencies for the Store to use, and
2. Create additional functions instead of just putting everything into the Store's builder.

The best way to do this is to create a class that acts as a simple wrapper for your store. By convention, it can
usually be called `Container`. Feel free to not use the provided interface, its only purpose is to provide a DSL.

```kotlin
private typealias Ctx = PipelineContext<CounterState, CounterIntent, CounterAction>

class CounterContainer(
    private val repo: CounterRepository,
) : Container<CounterState, CounterIntent, CounterAction> {

    override val store = store(Loading) {
        whileSubscribed { // installs a plugin that does something while there are subscribers
            repo.timer
                .onEach { produceState(it) } 
                .consume()
        }
    }

    // example custom function
    private fun Ctx.produceState(timer: Int) = updateState { DisplayingCounter(timer) }
}
```

* The `PipelineContext` is like an "environment" the store runs in. It is only available while the Store is running.
* Use the [IDE Plugin](https://plugins.jetbrains.com/plugin/25766-flowmvi) shortcut `fmvic` to generate a container and
  a store for you.

## Step 7: Start your store

!> Don't forget to start your Store! Store will do **nothing** unless it is started using the
`start(scope: CoroutineScope)` function or a scope is provided as a parameter to the builder.

Provide a coroutine scope with a lifecycle that matches the duration your Store should be accepting Intents
and running background jobs.

* On [Android](integrations/android.md), this will likely be a `viewModelScope`.
* On Desktop w/Compose, you can use `rememberCoroutineScope()`.
* On iOS, provide a scope manually or through a library.

#### Automatically:

```kotlin
fun counterStore(scope: CoroutineScope) = store(initial = Loading, scope = scope) { /* ... */ }
```

#### Separately:

```kotlin
fun counterStore() = store(initial = Loading) { /* ... */ }

// somewhere else
val store = counterStore()
store.start(lifecycleScope)
```

#### Manually

```kotlin
val scope = CoroutineScope()
val store = counterStore()

// start
store.start(scope)

// stop
scope.cancel()
// or to keep the scope alive
store.close()
```

### Step 8: Subscribe to your Store

The way you do this varies a lot based on what you use the store for and your app's UI framework, if any.
For this example, subscribing in Compose is extremely easy:

```kotlin
@Composable
fun CounterScreen(
    container: CounterContainer = DI.inject(),
) = with(container.store) {

        val state by subscribe { action ->
            when (action) {
                is ShowMessage -> {
                    /* ... */
                }
            }
        }

        CounterScreenContent(state)
    }

@Composable
fun IntentReceiver<CounterIntent>.CounterScreenContent(state: DisplayingCounterState) {
    /* ... */
}
```

* Use the [IDE Plugin](https://plugins.jetbrains.com/plugin/25766-flowmvi) shortcut `fmvis` to generate a composable
  screen for you.
* To learn more about FMVI in Compose, see [this guide](compose.md)
* To subscribe using Android Views, see [android guide](integrations/android.md)

## Next Steps

That's it! You have set up a feature using FlowMVI in ~100 lines of code.

Now you can start using the features of the library to write scalable business logic with plugins.

Continue learning by reading these articles:

1. Learn how to [install](plugins/prebuilt.md) and [create](plugins/custom.md) plugins.
2. Learn how to use FlowMVI with [compose](compose.md)
3. Learn how to [persist and restore state](plugins/savedstate.md)
4. Set up [remote debugging](plugins/debugging.md)
5. Learn how to use FlowMVI on [Android](integrations/android.md)
6. Get answers to common [questions](faq.md)
7. Explore the [sample app](https://github.com/respawn-app/FlowMVI/tree/master/sample/) for code examples
