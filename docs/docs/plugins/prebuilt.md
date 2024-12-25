---
sidebar_position: 1
---

# Getting started with plugins

FlowMVI is built entirely based on Plugins!
Plugins form a chain of responsibility (called _Pipeline_) and
execute _in the order they were installed_ into the Store.
This allows you to assemble business logic like a lego by placing the "bricks" in the order you want, and transparently
inject some logic into any store at any point.

Here's how the Plugin chain works:

![](/chart.png)

## Plugin Ordering

:::danger[The order of plugins matters! ]

Changing the order of plugins may completely change how your store works.
Plugins can replace, veto, consume, or otherwise change anything in the store.
They can close the store or swallow exceptions!

:::

Consider the following:

```kotlin
val broken = store(Loading) {
    reduce {

    }
    // ❌ - logging plugin will not log any intents
    // because they have been consumed by the reduce plugin
    enableLogging()
}

val working = store(Loading) {
    enableLogging()

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

## Prebuilt Plugins

FlowMVI comes with a whole suite of prebuilt plugins to cover the most common development needs.

Here's a full list:

* **Reduce Plugin** - process incoming intents. Install with `reduce { }`.
* **Init Plugin** - do something when the store is launched. Install with `init { }`.
* **Recover Plugin** - handle exceptions, works for both plugins and jobs. Install with `recover { }`.
* **While Subscribed Plugin** - run jobs when the `N`th subscriber of a store appears. Install
  with `whileSubscribed { }`.
* **Logging Plugin** - log events to a log stream of the target platform. Install with `enableLogging()`
* **Cache Plugin** - cache values in store's scope lazily and with the ability to suspend, binding them to the store's
  lifecycle. Install with `val value by cache { }`
* **Async cache plugin** - like `cache`, but returns a `Deferred` that can be awaited. Advantageous because it does not
  delay the store's startup sequence.
* **Job Manager Plugin** - keep track of long-running tasks, cancel and schedule them. Install with `manageJobs()`.
* **Await Subscribers Plugin** - let the store wait for a specified number of subscribers to appear before starting its
  work. Install with `awaitSubscribers()`.
* **Undo/Redo Plugin** - undo and redo any action happening in the store. Install with `undoRedo()`.
* **Disallow Restart Plugin** - disallow restarting the store if you do not plan to reuse it.
  Install with `disallowRestart()`.
* **Time Travel Plugin** - keep track of state changes, intents and actions happening in the store. Mostly used for
  testing, debugging and when building other plugins. Install with `val timeTravel = timeTravel()`
* **Consume Intents Plugin** - permanently consume intents that reach this plugin's execution order. Install with
  `consumeIntents()`.
* **Deinit Plugin** - run actions when the store is stopped.
* **Reset State Plugin** - reset the state of the store when it is stopped.
* **Saved State Plugin** - Save state somewhere else when it changes, and restore when the store starts.
  See [saved state](/plugins/savedstate.md) for details.
* **Remote Debugging Plugin** - connect to a remote debugger IDE Plugin / desktop app shipped with FlowMVI. See
  the [documentation](/plugins/debugging.md) to learn how to set up the environment.
* **Literally any plugin** - just call `install { }` and use the plugin's scope to hook up to store events.

All plugins are based on the essential callbacks that FlowMVI allows them to intercept.
The callbacks are explained on the [custom plugins](/plugins/custom.md) page.

Here's an explanation of how each default plugin works:

### Reduce Plugin

This is probably the most essential plugin in the library. Here's the full code of the plugin:

```kotlin
fun reducePlugin(
    consume: Boolean = true,
    name: String = ReducePluginName,
    reduce: PipelineContext.(intent: I) -> Unit,
) = plugin {
    this.name = name

    onIntent {
        reduce(it)
        it.takeUnless { consume }
    }
}
```

* This plugin simply executes `reduce` when it receives an intent.
* If you set `consume = true`, the plugin will **not** let other plugins installed after this one receive the intent.
  Set `consume = false` to install more than one reduce plugin.
* By default, you can see above that this plugin must be unique. Provide a custom name if you want to have multiple.

Install this plugin in your stores by using

```kotlin
val store = store(Loading) {

    reduce { intent ->

    }
}
```

You don't need "Reducers" with FlowMVI. Reducer is nothing more than a function.

### Init plugin

This plugin invokes a given (suspending) action **before** the Store starts, each time it starts.

Here's the full code (simplified a bit):

```kotlin
fun initPlugin(
    block: suspend PipelineContext.() -> Unit,
) = plugin {

    onStart(block)
}
```

Here are some interesting properties that apply to all plugins that use `onStart`:

* They are executed **each time** the store starts.
* They can suspend, and until **all** of them return, the store will **not handle any subscriptions, intents or any
  other actions**
* They have a `PipelineContext` receiver which allows you to send intents, side effects and launch jobs

:::warning[Do not suspend forever]

Do not collect long-running flows or suspend forever in this plugin as it not only prevents the store from starting,
but also operates in the lifecycle of the store, which is active even if there are no subscribers (UI is not visible).
It does not respect system lifecycle and navigation backstack logic.
Consider using `whileSubscribed` if you need lifecycle awareness.

:::

This plugin can be useful when you want to do something **before** the store is fully started.

Install the init plugin by calling

```kotlin
val store = store(Loading) {

    init { // this: PipelineContext

    }
}
```

### Recover plugin

Here's the full code of the plugin:

```kotlin
fun recoverPlugin(
    name: String? = null,
    recover: PipelineContext.(e: Exception) -> Exception?
) = plugin {
    this.name = name

    onException(recover)
}
```

This plugins executes `recover` lambda each time an exception happens in any of the store's callbacks, plugins or jobs
This callback is invoked asynchronously **after** the exception has been thrown and the job that threw it was cancelled.
With this plugin, you cannot continue the execution of the job because it has already ended.
If you return `null` from this plugin, this means that the exception was handled and it will be swallowed in this case.

This plugin can be useful to display an error message to the user, retry an operation, or report errors to analytics.

Install this plugin by using:

```kotlin
val store = store(Loading) {

    recover { e: Exception ->

        null
    }
}
```

### While Subscribed Plugin

This plugin launches a background job whenever the number of store subscribers reaches a minimum value (1 by default)
and automatically cancels it when that number drops below the minimum.

```kotlin
fun whileSubscribedPlugin(
    minSubscriptions: Int = 1,
    block: suspend PipelineContext.() -> Unit,
) = plugin {
    val job = SubscriptionHolder()
    onSubscribe { current ->
        when {
            current < minSubscriptions -> job.cancelAndJoin()
            job.isActive -> Unit // condition was already satisfied
            current >= minSubscriptions -> job.start(this) { block() } // new async job
        }
    }
    onUnsubscribe { current ->
        if (current < minSubscriptions) job.cancelAndJoin()
    }
    onStop { job.cancel() }
}
```

* This plugin is designed to suspend inside its `block` because it already launches a background job.
You can safely collect flows and suspend forever in the `block`.
* After the store is started, this plugin will begin receiving subscription events from the store.
* The **first time** the number of plugins reaches the minimum, the block that you provided will be run.
* The job will stay active until it either ends by itself or the number of subscriptions drops below the minimum.
* If the job has ended by itself, it will only be launched **after** the count of subscriptions has dropped below the
  minimum. I.e. it will not be relaunched each time an additional subscriber appears,
  but only when the condition is satisfied the next time again.

This plugin is useful for starting and stopping observation of some external data sources when the user can interact
with the app. For example, you may want to collect some flows and call `updateState` on each emission to update
the state you display to the user.

Install the plugin with:

```kotlin
val store = store(Loading) {

    whileSubscribed { // optionally provide the number of subs

    }
}
```

### Logging plugin

This plugin prints the events that happen in the store to the `logger` that you specified when you were creating the
store.

The default `PlatformStoreLogger` will print to:

* Logcat on Android
* NSLog on Apple platforms
* Console on Wasm and JS
* Stdout / Stderr on JVM
* Stdout on other platforms

---

* Tags are only used on Android, so on other platforms they will be appended as a part of the message.
* On platforms that do not support levels, an emoji will be printed instead
* Don't worry about heavy operations inside your `log { }` statements, the lambda is skipped if there is no logger.
* Use `NoOpStoreLogger` if you want to prevent any kind of logging, for example on production.

Install this plugin with:

```kotlin
val store = store(Loading) {

    enableLogging()
}
```

### Cache / Async Cache Plugins

Here's a simplified version of the code:

```kotlin
fun <T> cachePlugin(
    init: suspend PipelineContext.() -> T,
) = plugin {

    val value = CachedValue<T>(init)

    onStart { value.init() }

    onStop { value.clear() }
}
```

This plugin provides a delegate that is very similar to `lazy`, but the reference that the plugin holds is tied to the
lifecycle of the store, which means when the store starts, the value is initialized using the provided `init` parameter,
and when the store stops, it clears the reference to the value. If you use the `PipelineContext` inside, it will be
cancelled by the store itself.

By default, the entire store startup sequence will suspend until all values are initialized, but if you don't want that,
there is a second version of this plugin called `asyncCache` that returns a `Deferred` you can await. This one can
be very useful to initialize a lot of heavy stuff in parallel.

* You can create a `CachedValue` outside of the store if you need to access it outside of the store builder scope,
  but you **must** install the plugin using the value, and you must **not** try to access the value outside of the
  store's lifecycle, or the attempt will throw. To create it, use the `cached { }` delegate.
* You can access the value returned by `cache` in the `onStop` callback because the `onStop` is called in reverse plugin
  installation order.

This plugin is most useful:

* When you want to either suspend in the initializer (like a suspending `lazy`), in which case it will function
  similarly to `init` plugin
* When you want to use the `PipelineContext` (and its `CoroutineScope`) when initializing a value, for example with
  pagination or shared flows

Install this plugin using:

```kotlin
suspend fun produceTimer(): Flow<Int>

val store = store(Loading) {

    val timer by cache {
        produceTimer().stateIn(scope = this, initial = 0)
    }
}
```

or provide the value externally:

```kotlin
// do not access outside the store lifecycle
// need to specify type parameters - ambiguous
val value = cached<_, State, Intent, Action> { produceTimer() }

val store = store(Loading) {

    install(cachePlugin(value))
}
```

### Job Manager Plugin

FlowMVI provides a `JobManager` class that can store references to long-running `Job`s by an arbitrary key and manage
them. Job manager can then hook up to the store lifecycle events to cancel the jobs as appropriate:

```kotlin
fun <K : Any> jobManagerPlugin(
    manager: JobManager<K>,
    name: String? = JobManager.Name,
) = plugin {
    this.name = name

    onStop { manager.cancelAll() }
}
```

Examine the methods of the `JobManager` class to learn what it can do.

Create a job manager and immediately install it using:

```kotlin
enum class Jobs { Connection }

val store = store(Loading) {

    val jobs: JobManager<Jobs> = manageJobs()
}
```

Or provide the job manager externally:

```kotlin
val manager = JobManager<Jobs>()

val store = store(Loading) {

    manageJobs(manager)
}
```

Then register a job once you launch it:

```kotlin
val store = store(Loading) {

    val jobs = manageJobs()

    init {
        launch {
            websocket.connect()
        }.registerOrReplace(Jobs.Connection, jobs)
    }

    recover { e ->
        if (e is DeviceOfflineException) jobs.cancel(Connection)
        e
    }
}
```

### Await Subscribers Plugin

This plugin allows you to suspend until the store has reached a specified number of subscribers present.
To use it, you can create an instance of `SubscriberManager` and call `await` to suspend until the condition is met.

```kotlin
fun awaitSubscribersPlugin(
    manager: SubscriberManager,
    minSubs: Int = 1,
    allowResubscription: Boolean = true,
    suspendStore: Boolean = true,
    timeout: Duration = Duration.INFINITE,
    name: String = SubscriberManager.Name,
) = plugin {
    /* ... */
}
```

* Specify `minSubs` to determine the minimum number of subscribers to reach.
* Choose `suspendStore` to block all store operations until the condition is met. If you pass `false`, only the code
  that explicitly calls `await()` will suspend.
* If you pass the `allowResubscription` parameter, then after they leave, the state will reset and you
  can call `await()` again.
* Specify a `timeout` duration or `complete()` the job manually if you want to finish early.

### Undo/Redo Plugin

Undo/Redo plugin allows you to create and manage a queue of operations that can be undone and repeated.

```kotlin
fun undoRedoPlugin(
    undoRedo: UndoRedo,
    name: String? = null,
    resetOnException: Boolean = true,
) = plugin {
    this.name = name

    onStop { undoRedo.reset() }

    if (resetOnException) onException {
        it.also { undoRedo.reset() }
    }
}
```

The events will be reset when store is stopped and (optionally) when an exception occurs.

You can observe the queue of events for example in `whileSubscribed`:

```kotlin
val store = store(Loading) {

    val undoRedo = undoRedo(queueSize = 10)

    whileSubscribed {
        undoRedo.queue.onEach { (i, canUndo, canRedo) ->
            updateState {
                copy(index = i, canUndo = canUndo, canRedo = canRedo)
            }
        }.collect()
    }

    reduce { intent ->
        when (intent) {
            is ClickedRedo -> undoRedo.redo()
            is ClickedUndo -> undoRedo.undo()
            is ChangedInput -> undoRedo(
                redo = { updateState { copy(input = intent.current) } },
                undo = { updateState { copy(input = intent.previous) } },
            )
        }
    }
}
```

This plugin can be useful whenever you are implementing an "editor" type functionality, but currently not fully
implemented to handle all edge cases.

### Time Travel plugin

Time travel records all Intents, Actions, State Changes, subscription events, starts and stops of the store.

It's mostly useful for debugging, logging and other technical tasks.
For example, FlowMVI's testing DSL embeds a time travel plugin when testing the store.

```kotlin
fun timeTravelPlugin(
    timeTravel: TimeTravel,
    name: String = TimeTravel.Name,
) = plugin {
    this.name = name
    /* ... */
}
```

Install the plugin using:

```kotlin
val store = store(Loading) {
    val timeTravel = timeTravel()

    init {
        assert(timeTravel.starts == 1)
    }
}
```

### Deinit plugin

This one is a simple DSL for calling `onStop`:

```kotlin
fun deinitPlugin(
    block: ShutdownContext.(e: Exception?) -> Unit
) = plugin { onStop { block(it) } }
```

It is useful in combination with `cache` plugin, or if you need to clean up some external resource or set the state
as the store stops. It is called reliably (but synchronously) on store shutdown.

The exception will be `null` on normal shutdowns, and non-null when there was an error, just before the store throws.

Install it by simply calling `deinit { }`.

### Reset State Plugin

```kotlin
public fun resetStatePlugin() = plugin {
    this.name = "ResetStatePlugin"
    onStop {
        updateStateImmediate { config.initial }
    }
}
```

This plugin simply resets state back to `initial` once the store is stopped.
The moment when this happens is determined by the plugin installation order. Install with `resetStateOnStop()`

### Or Create Your Own

As if having so many plugins was not great in itself, the true power of the library is in creating custom plugins.

Learn how to do that in the [next guide](/plugins/custom.md)
