# Get Started with FlowMVI

Here's how the library works at a glance:

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
flowmvi-debugger-client = { module = "pro.respawn.flowmvi:debugger-plugin", version.ref = "flowmvi" }
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
    // remote debugging client
    androidDebugImplementation("pro.respawn.flowmvi:debugger-plugin:$flowmvi")
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

Then configure your kotlin compilation to target JVM 11 in your root `build.gradle.kts`:

```kotlin
allprojects {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
}
```

And in your module-level gradle files, set:
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

### 1.3 Configure Compose

If you are using compose, set up stability definitions for your project.

<details>
<summary>stability_definitions.txt</summary>

```text
pro.respawn.flowmvi.api.MVIIntent
pro.respawn.flowmvi.api.MVIState
pro.respawn.flowmvi.api.MVIAction
pro.respawn.flowmvi.api.Store
pro.respawn.flowmvi.api.Container
pro.respawn.flowmvi.api.ImmutableStore
pro.respawn.flowmvi.dsl.LambdaIntent
pro.respawn.flowmvi.api.SubscriberLifecycle
pro.respawn.flowmvi.api.IntentReceiver
```

</details>

Then configure compose compiler to account for the definitions in your root `build.gradle.kts`:

<details>
<summary>/build.gradle.kts</summary>

```kotlin
allprojects {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:stabilityConfigurationPath=" +
                        "${rootProject.rootDir.absolutePath}/stability_definitions.txt"
            )
        }
    }
}
```

</details>

Now the states/intents you create will be stable in compose. Immutability of these classes is already required by the
library, so this will ensure you get the best performance.

## Step 2: Choose your style

FlowMVI supports both MVI (strict model-driven logic) and the MVVM+ (functional, lambda-driven logic) styles.
It's preferable to choose one style and use it throughout your project.
Each style has its own pros and cons, so choosing can be hard.
So please consider the following comparison:

### MVI style:

| Pros 👍                                                                                              | Cons 👎                                                                                                                         |
|------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| Greater separation of concerns as intent handling logic is strictly contained in the store's scope   | Boilerplatish: some intents will need to be duplicated for multiple screens, resulting in some amount of copy-paste             |
| Verbose and readable - easily understand which intent does what judging by the contract              | Hard to navigate in the IDE. You have to jump twice: first to the declaration, and then to usage, to see the code of the intent |
| Intents can be decomposed into sealed families, subclassed,  delegated, have properties or functions | Class explosion - class for every event may result in 50+ model classes per screen easily                                       |
| Intents can be resent inside the store - by sending an intent while handling another intent          | Sealed classes work worse for some platforms, for example, in Swift, Enums are not used and names are mangled                   |

### MVVM+ style:

| Pros 👍                                                                                                 | Cons 👎                                                                    |
|:--------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------|
| Elegant syntax - open a lambda block and write your logic there. Store's code remains clean             | You have to use `ImmutableStore` interface to not leak the store's context |
| Easily navigate to and see what an intent does in one click                                             | Lambdas are less performant than regular intents                           |
| Easier to support on other platforms if handled correctly (not exposing store's logic in platform code) | Some plugins will become useless, such as logging/time travel/analytics    |
| Get rid of all Intent classes entirely, avoid class explosion                                           | Intents cannot be composed, delegated and organized into families          |

If you decide to use the MVVM+ style, consider using `ImmutableStore` interface that won't let external code send 
intents. This will prevent leaking the context of the store to subscribers.

## Step 3: Describe your Contract

<details>
<summary>Click for general advice on how to define a contract</summary>

Describing the contract first makes building the logic easier because this helps make your business logic declarative.
To define your contract, ask yourself the following:

1. What can be shown at what times? Can the page be empty? Can it be loading? Can errors happen? -
   this will define your state family.
2. What elements can be shown on this screen, for each state? - this will be your state properties.
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
    * Prefer using state instead of events if possible if you are able to declaratively handle an event. [Read more here](https://proandroiddev.com/viewmodel-events-as-state-are-an-antipattern-35ff4fbc6fb6).
    * Do not confuse States with Actions! Actions are **one-off, "fire and forget" events** that cannot be tracked after being sent.
    * Actions are **sent and received sequentially**.
    * Actions are sent from Store to the UI. Intents are sent in the other direction.
    * Actions are not strictly guaranteed to be received by the subscriber, so do not use them for crucial elements of
      the logic.

</details>

```kotlin
// Must be comparable and immutable. Automatically marked as stable in Compose
sealed interface CounterState : MVIState {
    data object Loading : CounterState
    data class Error(val e: Exception) : CounterState
    data class DisplayingCounter(val counter: Int) : CounterState
}

// MVI Style Intents
sealed interface CounterIntent : MVIIntent {

    data object ClickedNext : CounterIntent

    @JvmInline
    value class ChangedCounter(val value: Int) : CounterIntent

    data class GrantedPermission(val granted: Boolean, val permission: String) : CounterIntent
}

// MVVM+ Style Intents
typealias CounterIntent = LambdaIntent<CounterState, CounterAction>

sealed interface CounterAction : MVIAction {

    data class ShowMessage(val message: String) : CounterAction
}
```

* If your store does not have a `State`, you can use an `EmptyState` object.
* If your store does not have side-effects, use `Nothing` in place of the side-effect type.

## Step 4: Define your store

Here's a full list of things that can be done when configuring the store:

```kotlin
val store = store<CounterState, CounterIntent, CounterAction>(Loading) { // set initial state

    configure {
        var debuggable = false
        var name: String? = null
        var parallelIntents = false
        var coroutineContext: CoroutineContext = EmptyCoroutineContext
        var actionShareBehavior = ActionShareBehavior.Distribute()
        var onOverflow = BufferOverflow.DROP_OLDEST
        var intentCapacity = Channel.UNLIMITED
        var atomicStateUpdates = true
        var allowIdleSubscriptions: Boolean? = null
        var logger: StoreLogger? = null
    }

    fun install(vararg plugins: LazyPlugin<S, I, A>)
    fun install(block: LazyPluginBuilder<S, I, A>.() -> Unit)
}
```

* `debuggable` - Settings this to true enables additional store validations and debug logging. The store will check your
  subscription events, launches/stops, and plugins for validity, as well as print logs to the system console.
* `name` - Set the future name of the store. Needed for debug, logging, comparing and injecting stores
* `parallelIntents` - Declare that intents must be processed in parallel. Intents may still be dropped according to the
  `onOverflow` param.
* `coroutineContext` - A coroutine context override for the store. This context will be merged with the one the store
  was launched with (e.g. `viewModelScope`). All store operations will be launched in that context by default.
* `actionShareBehavior` - Define how the store handles and sends actions. Choose one of the following:
    * `Distribute` - send side effects in a fan-out FIFO fashion to one subscriber at a time (default).
    * `Share` - share side effects between subscribers using a `SharedFlow`.
      !> If an event is sent and there are no subscribers, the event will be lost!
    * `Restrict` - Allow only **one** subscription event per whole lifecycle of the store. If you want to subscribe
      again, you will have to re-create the store.
    * `Disable` - Disable side effects.
* `onOverflow` - Designate behavior for when the store's intent queue overflows. Choose from:
    * `SUSPEND` - Suspend on buffer overflow.
    * `DROP_OLDEST` - Drop **the oldest** value in the buffer on overflow, add the new value to the buffer, do not
      suspend (default).
    * `DROP_LATEST` - Drop **the latest** value that is being added to the buffer right now on buffer overflow (so that
      buffer contents stay the same), do not suspend.
* `intentCapacity` - Designate the maximum capacity of store's intent queue. This should be either:
    * A positive value of the buffer size
    * `UNLIMITED` - unlimited buffer (default)
    * `CONFLATED` - A buffer of 1
    * `RENDEZVOUS` - Zero buffer (all events not ready to be processed are dropped)
    * `BUFFERED` - Default system buffer capacity
* `atomicStateUpdates` - Enables transaction serialization for state updates, making state updates atomic and
  suspendable. Synchronizes state updates, allowing only **one** client to read and/or update the state at a time. All
  other clients that attempt to get the state will wait in a FIFO queue and suspend the parent coroutine. For one-time
  usage of non-atomic updates, see `useState`. Has a small performance impact because of coroutine context switching and
  mutex usage when enabled.
* `allowIdleSubscriptions` - A flag to indicate that clients may subscribe to this store even while it is not started.
  If you intend to stop and restart your store while the subscribers are present, set this to `true`. By default, will
  choose a value based on `debuggable` parameter.
* `logger` - An instance of `StoreLogger` to use for logging events. By default, the value is chosen based on
  the `debuggable` parameter:
    * `PlatformStoreLogging` that logs to the primary log stream of the system (e.g. Logcat on Android).
    * `NoOpStoreLogging` - if `debuggable` is false, logs will not be printed.

Some interesting properties of the store:

* Store can be launched, stopped, and relaunched again as many times as you want.
  Use `close()`, or cancel the job returned from `start()` to stop the store.
* Store's subscribers will **not** wait until the store is launched when they subscribe to the store.
  Such subscribers will not receive state updates or actions. Don't forget to start the store.
* Stores are created eagerly usually, but the store *can* be lazy. There is `lazyStore()` for that.

## Step 5: Install plugins

FlowMVI is built entirely based on plugins!
**Everything** in FlowMVI 2.0 is a plugin. This includes handling errors and even **reducing intents**.

Call the `install` function using a prebuilt plugin, or use a lambda to create and install a plugin on the fly.

For every store, you'll likely want to install a few plugins.
Prebuilt plugins come with a nice dsl when building a store. Check out the [plugins](plugins.md) page to learn how
to use them.

!> The order of plugins matters! Changing the order of plugins may completely change how your store works.
Plugins can replace, veto, consume or otherwise change anything in the store.
They can close the store or swallow exceptions!

Consider the following:

```kotlin
val broken = store(Loading) {
    reduce {

    }
    // ❌ - logging plugin will not log any intents
    // because they have been consumed by the reduce plugin
    install(consoleLoggingPlugin())
}

val working = store(Loading) {
    install(consoleLoggingPlugin())

    reduce {
        // ✅ - logging plugin will get the intent before reduce() is run, and it does not consume the intent
    }
}
```

That example was simple, but this rule can manifest in other, not so obvious ways. Consider the following:

```kotlin
val broken = store(Loading) {

    serializeState() // ‼️ restores state on start

    init {
        updateState {
            Loading // 🤦‍ and the state is immediately overwritten
        }
    }

    // this happened because serializeState() uses onStart() under the hood, and init does too.
    // Init is run after serializeState because it was installed later.
}
// or
val broken = store(Loading) {

    install(customUndocumentedPlugin()) // ‼️ you don't know what this plugin does

    reduce {
        // ❌ intents are not reduced because the plugin consumed them
    }
    init {
        updateState {
            // ❌ states are not changed because the plugin veto'd the change
        }
        action(MyAction) // ❌ actions are replaced with something else
    }
}
```
So make sure to consider how your plugins affect the store's logic when using and writing them.

The discussion above warrants another note.

### Step 6: Create, inject and provide dependencies

You'll likely want to provide some dependencies for the store to use and to create additional functions instead of just
putting all code into the store's builder.

The best way to do this is to create a class that acts as a simple wrapper for your store. By convention, it can
usually be called `Container`. Feel free to not use the provided interface, its only purpose is to act as a marker.

```kotlin
private typealias Ctx = PipelineContext<CounterState, CounterIntent, CounterAction>

class CounterContainer(
    private val repo: CounterRepository,
) : Container<CounterState, CounterIntent, CounterAction> {

    override val store = store(Loading) {
        whileSubscribed {
            repo.timer
                .onEach { produceState(it) } 
                .consume()
        }
    }

    // example custom function
    private fun Ctx.produceState(timer: Int) = updateState { DisplayingCounter(timer) }
}
```

### Step 7: Start and subscribe to your store

!> Don't forget to start your store! Store will do nothing unless it is started using the `start(scope: CoroutineScope)`
function. Provide a coroutine scope with a lifecycle that matches the duration your store should be accepting intents
and running background jobs. For  [android](android.md) this will be `viewModelScope` for example.
On desktop, you can use `rememberCoroutineScope()`. On iOS, provide an application lifecycle scope.

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
store.stop()
```

To subscribe to the store, regardless of your platform see [this guide](android.md)

### Next steps:

* Learn how to create custom [plugins](plugins.md)
* Learn how to use DI and [Android-specific features](android.md)
* Learn how to [persist and restore state](savedstate.md)
* Get answers to common [questions](faq.md)
* Set up [remote debugging](debugging.md)
* [Read an article](https://medium.com/@Nek.12/success-story-how-flowmvi-has-changed-the-fate-of-our-project-3c1226890d67)
  about how our team has used the library to improve performance and stability of our app, with practical examples.
* Explore
  the [sample app](https://github.com/respawn-app/FlowMVI/tree/master/sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample)
    * Want more samples? Explore how we created
      a [multiplatform debugger app](https://github.com/respawn-app/FlowMVI/tree/34236773e21e7138a330d7d0fb6c5d0eba21b61e/debugger/server/src/commonMain/kotlin/pro/respawn/flowmvi/debugger/server)
      for FlowMVI using... FlowMVI itself.
