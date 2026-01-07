# FlowMVI plugin and decorator signatures (compile-ready)

This file lists core plugins and decorators with exact signatures and packages. Each entry includes a short description to avoid cross-referencing.

Full doc available by running `curl -S https://opensource.respawn.pro/FlowMVI/plugins/prebuilt.md`.

## Core plugins (package: `pro.respawn.flowmvi.plugins`)

### Reduce

Processes incoming intents. Use as the main intent reducer in MVI style. `consume = true` stops the chain after reduce.

```kotlin
package pro.respawn.flowmvi.plugins

public const val ReducePluginName: String = "ReducePlugin"
public typealias Reduce<S, I, A> = suspend PipelineContext<S, I, A>.(intent: I) -> Unit

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.reduce(
    consume: Boolean = true,
    name: String = ReducePluginName,
    @BuilderInference crossinline reduce: Reduce<S, I, A>,
): Unit

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> reducePlugin(
    consume: Boolean = true,
    name: String = ReducePluginName,
    @BuilderInference crossinline reduce: Reduce<S, I, A>,
): StorePlugin<S, I, A>
```

### Init

Runs work at store start. Use for startup work that must run before the store processes intents.

```kotlin
package pro.respawn.flowmvi.plugins

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.init(
    name: String? = null,
    @BuilderInference block: suspend PipelineContext<S, I, A>.() -> Unit
): Unit

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> initPlugin(
    name: String? = null,
    @BuilderInference block: suspend PipelineContext<S, I, A>.() -> Unit,
): StorePlugin<S, I, A>

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.asyncInit(
    context: CoroutineContext = EmptyCoroutineContext,
    name: String? = null,
    crossinline block: suspend PipelineContext<S, I, A>.() -> Unit
): Unit
```

### Recover

Handles exceptions thrown by store jobs or plugin callbacks. Return `null` to swallow or the exception to propagate.

```kotlin
package pro.respawn.flowmvi.plugins

public typealias Recover<S, I, A> = suspend PipelineContext<S, I, A>.(e: Exception) -> Exception?

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.recover(
    name: String? = null,
    @BuilderInference recover: Recover<S, I, A>,
): Unit

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> recoverPlugin(
    name: String? = null,
    @BuilderInference recover: Recover<S, I, A>
): StorePlugin<S, I, A>
```

### WhileSubscribed

Runs jobs only while the store has subscribers. Great for lifecycle-aware flows.

```kotlin
package pro.respawn.flowmvi.plugins

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> whileSubscribedPlugin(
    name: String? = null,
    minSubscriptions: Int = 1,
    stopDelay: Duration = 1.seconds,
    @BuilderInference crossinline block: suspend PipelineContext<S, I, A>.() -> Unit,
): StorePlugin<S, I, A>

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.whileSubscribed(
    name: String? = null,
    minSubscriptions: Int = 1,
    stopDelay: Duration = 1.seconds,
    @BuilderInference crossinline block: suspend PipelineContext<S, I, A>.() -> Unit,
): Unit
```

### Logging

Logs intents/actions/states/exceptions using store logger. Use in debug builds or for observability.

```kotlin
package pro.respawn.flowmvi.plugins

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.enableLogging(
    tag: String? = null,
    level: StoreLogLevel? = null,
    name: String? = null,
    logger: StoreLogger? = null,
): Unit

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> loggingPlugin(
    tag: String? = null,
    level: StoreLogLevel? = null,
    name: String? = null,
    logger: StoreLogger? = null,
): LazyPlugin<S, I, A>
```

### Cache

Caches a value scoped to store lifecycle. Use for lazily-initialized dependencies that depend on PipelineContext.

```kotlin
package pro.respawn.flowmvi.plugins

public class CachedValue<out T, S : MVIState, I : MVIIntent, A : MVIAction>

public fun <T, S : MVIState, I : MVIIntent, A : MVIAction> cached(
    @BuilderInference init: suspend PipelineContext<S, I, A>.() -> T,
): CachedValue<T, S, I, A>

@FlowMVIDSL
public fun <T, S : MVIState, I : MVIIntent, A : MVIAction> cachePlugin(
    value: CachedValue<T, S, I, A>,
    name: String? = null,
): StorePlugin<S, I, A>

@FlowMVIDSL
public fun <T, S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.cache(
    name: String? = null,
    @BuilderInference init: suspend PipelineContext<S, I, A>.() -> T,
): CachedValue<T, S, I, A>
```

### Async cache

Like `cache`, but initialization runs asynchronously and returns a Deferred. Use when startup must not be blocked.

```kotlin
package pro.respawn.flowmvi.plugins

public suspend operator fun <T> Deferred<T>.invoke(): T

@FlowMVIDSL
public inline fun <T, S : MVIState, I : MVIIntent, A : MVIAction> asyncCached(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.UNDISPATCHED,
    crossinline init: suspend PipelineContext<S, I, A>.() -> T,
): CachedValue<Deferred<T>, S, I, A>

@FlowMVIDSL
public inline fun <T, S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.asyncCache(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.UNDISPATCHED,
    crossinline init: suspend PipelineContext<S, I, A>.() -> T,
): CachedValue<Deferred<T>, S, I, A>
```

### Await subscribers

Blocks processing until a minimum subscriber count appears (optionally with timeout). Use when work should only run with UI attached.

```kotlin
package pro.respawn.flowmvi.plugins

public class SubscriberManager {
    public suspend fun await()
    public fun complete()
    public suspend fun completeAndWait(): Unit?
}

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.awaitSubscribers(
    manager: SubscriberManager = SubscriberManager(),
    minSubs: Int = 1,
    suspendStore: Boolean = true,
    timeout: Duration = Duration.INFINITE,
    name: String = SubscriberManager.Name,
): SubscriberManager

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> awaitSubscribersPlugin(
    manager: SubscriberManager,
    minSubs: Int = 1,
    suspendStore: Boolean = true,
    timeout: Duration = Duration.INFINITE,
    name: String = SubscriberManager.Name,
): StorePlugin<S, I, A>
```

### Consume intents

Consumes remaining intents without handling. Use as a tail plugin when `reduce(consume = false)` is used.

```kotlin
package pro.respawn.flowmvi.plugins

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> consumeIntentsPlugin(
    name: String = "ConsumeIntents",
): StorePlugin<S, I, A>

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.consumeIntents(
    name: String = "ConsumeIntents",
): Unit
```

### Child store lifecycle

Starts/stops child stores alongside a parent store. Use for composition and shared lifecycle.

```kotlin
package pro.respawn.flowmvi.plugins

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> childStorePlugin(
    children: Iterable<Store<*, *, *>>,
    force: Boolean? = null,
    blocking: Boolean = false,
    name: String? = null,
): StorePlugin<S, I, A>

@FlowMVIDSL
public fun StoreBuilder<*, *, *>.installChild(
    children: Iterable<Store<*, *, *>>,
    force: Boolean? = null,
    blocking: Boolean = false,
    name: String? = null,
): Unit

@FlowMVIDSL
public fun StoreBuilder<*, *, *>.installChild(
    first: Store<*, *, *>,
    vararg other: Store<*, *, *>,
    force: Boolean? = null,
    blocking: Boolean = false,
    name: String? = null,
): Unit

@FlowMVIDSL
public infix fun StoreBuilder<*, *, *>.hasChild(other: Store<*, *, *>): Unit
```

### Job manager

Tracks, cancels, and replaces long-running jobs scoped to store lifecycle.

```kotlin
package pro.respawn.flowmvi.plugins

@FlowMVIDSL
public fun <K : Any> Job.register(manager: JobManager<K>, key: K): Job

@FlowMVIDSL
public suspend fun <K : Any> Job.registerOrReplace(
    manager: JobManager<K>,
    key: K,
): Job?

@FlowMVIDSL
public fun <K : Any, S : MVIState, I : MVIIntent, A : MVIAction> jobManagerPlugin(
    manager: JobManager<K>,
    name: String? = JobManager.Name,
): StorePlugin<S, I, A>

@FlowMVIDSL
public fun <K : Any, A : MVIAction, I : MVIIntent, S : MVIState> StoreBuilder<S, I, A>.manageJobs(
    jobs: JobManager<K> = JobManager(),
    name: String = JobManager.Name
): JobManager<K>
```

### Deinit / onStop

Runs cleanup when the store closes. Use for resource disposal that must run even on exceptions.

```kotlin
package pro.respawn.flowmvi.plugins

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> deinitPlugin(
    crossinline block: ShutdownContext<S, I, A>.(e: Exception?) -> Unit
): StorePlugin<S, I, A>

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.deinit(
    crossinline block: ShutdownContext<S, I, A>.(e: Exception?) -> Unit
): Unit
```

### Disallow restart

Ensures the store can only be started once. Useful for one-shot store lifecycles.

```kotlin
package pro.respawn.flowmvi.plugins

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> disallowRestartPlugin(): StorePlugin<S, I, A>

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.disallowRestart(): Unit
```

### Reset state on stop

Resets state to initial when the store stops. Install early if you want clean re-entry.

```kotlin
package pro.respawn.flowmvi.plugins

public fun <S : MVIState, I : MVIIntent, A : MVIAction> resetStatePlugin(): StorePlugin<S, I, A>

public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.resetStateOnStop(): Unit
```

### Time travel

Records state, intent, action history. Useful for debugging and remote debugger.

```kotlin
package pro.respawn.flowmvi.plugins

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> timeTravelPlugin(
    timeTravel: TimeTravel<S, I, A>,
    name: String = TimeTravel.Name,
): StorePlugin<S, I, A>

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.timeTravel(
    timeTravel: TimeTravel<S, I, A>,
    name: String = TimeTravel.Name,
): Unit

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.timeTravel(
    historySize: Int = TimeTravel.DefaultHistorySize,
    name: String = TimeTravel.Name,
): TimeTravel<S, I, A>
```

### Undo/Redo

Tracks state transitions to allow undo/redo. Keep the returned `UndoRedo` to trigger operations.

```kotlin
package pro.respawn.flowmvi.plugins

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> undoRedoPlugin(
    undoRedo: UndoRedo,
    name: String? = null,
    resetOnException: Boolean = true,
): StorePlugin<S, I, A>

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.undoRedo(
    maxQueueSize: Int,
    name: String? = null,
    resetOnException: Boolean = true,
): UndoRedo
```

## Store delegation / composition (package: `pro.respawn.flowmvi.plugins.delegate`)

Delegates another store’s state/actions into a principal store. Use for composing state across stores.

```kotlin
package pro.respawn.flowmvi.plugins.delegate

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction, CS : MVIState, CI : MVIIntent, CA : MVIAction>
storeDelegatePlugin(
    delegate: StoreDelegate<CS, CI, CA>,
    name: String? = delegate.name,
    start: Boolean = true,
    blocking: Boolean = false,
    consume: ChildConsume<S, I, A, CA>? = null,
): StorePlugin<S, I, A>

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction, CS : MVIState, CI : MVIIntent, CA : MVIAction>
StoreBuilder<S, I, A>.delegate(
    store: Store<CS, CI, CA>,
    mode: DelegationMode = DelegationMode.Default,
    name: String? = store.name?.let { "${it}DelegatePlugin" },
    start: Boolean = true,
    blocking: Boolean = false,
    consume: ChildConsume<S, I, A, CA>? = null,
): StoreDelegate<CS, CI, CA>
```

## Decorators (package: `pro.respawn.flowmvi.decorators`)

Decorators wrap the whole plugin chain and can short-circuit it. Use when you need to control or retry the chain itself.

### Debounce intents

Debounces intents like `flow.debounce`, forwarding only the last intent after a quiet period.

```kotlin
package pro.respawn.flowmvi.decorators

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> debounceIntentsDecorator(
    timeout: Duration,
    name: String? = "DebounceIntents",
): PluginDecorator<S, I, A>

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> debounceIntentsDecorator(
    name: String? = "DebounceIntents",
    timeoutSelector: suspend PipelineContext<S, I, A>.(I) -> Duration,
): PluginDecorator<S, I, A>

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.debounceIntents(
    timeout: Duration,
    name: String? = "DebounceIntents",
): Unit

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.debounceIntents(
    name: String? = "DebounceIntents",
    timeoutSelector: suspend PipelineContext<S, I, A>.(I) -> Duration,
): Unit
```

### Conflate intents/actions

Drops repeated intents/actions based on equality, keeping only the latest distinct event.

```kotlin
package pro.respawn.flowmvi.decorators

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> conflateIntentsDecorator(
    name: String? = "ConflateIntents",
    crossinline compare: ((it: I, other: I) -> Boolean) = MVIIntent::equals,
): PluginDecorator<S, I, A>

@ExperimentalFlowMVIAPI
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> conflateActionsDecorator(
    name: String? = "ConflateActions",
    crossinline compare: ((it: A, other: A) -> Boolean) = MVIAction::equals,
): PluginDecorator<S, I, A>

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.conflateIntents(
    name: String? = "ConflateIntents",
    crossinline compare: (it: I, other: I) -> Boolean = MVIIntent::equals,
): Unit

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.conflateActions(
    name: String? = "ConflateActions",
    crossinline compare: (it: A, other: A) -> Boolean = MVIAction::equals,
): Unit
```

### Batch intents

Batches intents by time or amount and flushes them in order. Use to reduce intent processing overhead.

```kotlin
package pro.respawn.flowmvi.decorators

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> batchIntentsDecorator(
    mode: BatchingMode,
    queue: BatchQueue<I> = BatchQueue(),
    name: String? = "BatchIntentsDecorator",
    onUnhandledIntent: suspend PipelineContext<S, I, A>.(intent: I) -> Unit = {}
): PluginDecorator<S, I, A>

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.batchIntents(
    mode: BatchingMode,
    name: String? = "BatchIntentsDecorator",
    onUnhandledIntent: suspend PipelineContext<S, I, A>.(intent: I) -> Unit = {}
): BatchQueue<I>
```

### Intent timeout

Fails intents that do not finish within a timeout. Use to avoid stuck processing.

```kotlin
package pro.respawn.flowmvi.decorators

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> intentTimeoutDecorator(
    timeout: Duration,
    name: String? = "IntentTimeout",
    onTimeout: suspend PipelineContext<S, I, A>.(I) -> I? = { throw StoreTimeoutException(timeout) },
): PluginDecorator<S, I, A>

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.timeoutIntents(
    timeout: Duration,
    name: String? = "IntentTimeout",
    onTimeout: suspend PipelineContext<S, I, A>.(I) -> I? = { throw StoreTimeoutException(timeout) },
): Unit
```

### Retry intents/actions

Retries the entire plugin chain for intents/actions based on a strategy and selector.

```kotlin
package pro.respawn.flowmvi.decorators

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> retryIntentsDecorator(
    strategy: RetryStrategy,
    name: String? = null,
    crossinline selector: (intent: I, e: Exception) -> Boolean = { _, _ -> true },
): PluginDecorator<S, I, A>

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> retryActionsDecorator(
    strategy: RetryStrategy,
    name: String? = null,
    crossinline selector: (action: A, e: Exception) -> Boolean = { _, _ -> true },
): PluginDecorator<S, I, A>

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.retryIntents(
    strategy: RetryStrategy = RetryStrategy.Default,
    name: String? = null,
    crossinline selector: (intent: I, e: Exception) -> Boolean = { _, _ -> true },
): Unit

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.retryActions(
    strategy: RetryStrategy = RetryStrategy.Default,
    name: String? = null,
    crossinline selector: (action: A, e: Exception) -> Boolean = { _, _ -> true },
): Unit
```

## Metrics (package: `pro.respawn.flowmvi.metrics.dsl`)

Collects and reports metrics snapshots. Use for performance/usage instrumentation.

```kotlin
package pro.respawn.flowmvi.metrics.dsl

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> metricsDecorator(
    metrics: DefaultMetrics<S, I, A>,
    name: String? = "MetricsDecorator"
): PluginDecorator<S, I, A>

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.collectMetrics(
    metrics: DefaultMetrics<S, I, A>,
    name: String? = "MetricsDecorator"
): Metrics

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.collectMetrics(
    reportingScope: CoroutineScope,
    offloadContext: CoroutineContext = Dispatchers.Default,
    windowSeconds: Int = 60,
    emaAlpha: Double = 0.1,
    clock: Clock = Clock.System,
    bucketDuration: Duration = 1.seconds,
    timeSource: TimeSource = TimeSource.Monotonic,
    name: String? = "MetricsDecorator"
): DefaultMetrics<S, I, A>

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> metricsReporter(
    metrics: Metrics,
    offloadScope: CoroutineScope,
    offloadContext: CoroutineContext = Dispatchers.Default,
    interval: Duration = 30.seconds,
    flushOnStop: Boolean = true,
    name: String? = "MetricsReporter",
    sink: MetricsSink,
): StorePlugin<S, I, A>

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> metricsReporter(
    builder: DefaultMetrics<S, I, A>,
    interval: Duration = 30.seconds,
    flushOnStop: Boolean = true,
    name: String? = "MetricsReporter",
    sink: MetricsSink,
): StorePlugin<S, I, A>

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.reportMetrics(
    metrics: Metrics,
    offloadScope: CoroutineScope,
    offloadContext: CoroutineContext = Dispatchers.Default,
    interval: Duration = 30.seconds,
    flushOnStop: Boolean = true,
    name: String? = "MetricsReporter",
    sink: MetricsSink,
): Unit

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.reportMetrics(
    metrics: DefaultMetrics<S, I, A>,
    interval: Duration = 30.seconds,
    flushOnStop: Boolean = true,
    name: String? = "MetricsReporter",
    sink: MetricsSink,
): Unit
```

## Remote debugging

### Default HttpClient (package: `pro.respawn.flowmvi.debugger.plugin`)

Creates a remote debugging plugin using the built-in HttpClient. Use only in debug builds.

```kotlin
package pro.respawn.flowmvi.debugger.plugin

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> debuggerPlugin(
    historySize: Int = DebuggerDefaults.DefaultHistorySize,
    host: String = DebuggerDefaults.ClientHost,
    port: Int = DebuggerDefaults.Port,
    reconnectionDelay: Duration = DebuggerDefaults.ReconnectionDelay,
): LazyPlugin<S, I, A>

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.enableRemoteDebugging(
    historySize: Int = DebuggerDefaults.DefaultHistorySize,
    host: String = DebuggerDefaults.ClientHost,
    port: Int = DebuggerDefaults.Port,
    reconnectionDelay: Duration = DebuggerDefaults.ReconnectionDelay,
): Unit
```

### Custom HttpClient (package: `pro.respawn.flowmvi.debugger.client`)

Use when you must provide a custom Ktor client. Also requires a TimeTravel plugin or history size.

```kotlin
package pro.respawn.flowmvi.debugger.client

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> debuggerPlugin(
    client: HttpClient,
    timeTravel: TimeTravel<S, I, A>,
    host: String = DebuggerDefaults.ClientHost,
    port: Int = DebuggerDefaults.Port,
    reconnectionDelay: Duration = DebuggerDefaults.ReconnectionDelay,
): LazyPlugin<S, I, A>

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> debuggerPlugin(
    client: HttpClient,
    historySize: Int = DefaultHistorySize,
    host: String = DebuggerDefaults.ClientHost,
    port: Int = DebuggerDefaults.Port,
    reconnectionDelay: Duration = DebuggerDefaults.ReconnectionDelay,
): LazyPlugin<S, I, A>

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.enableRemoteDebugging(
    client: HttpClient,
    historySize: Int = DefaultHistorySize,
    host: String = DebuggerDefaults.ClientHost,
    port: Int = DebuggerDefaults.Port,
    reconnectionDelay: Duration = DebuggerDefaults.ReconnectionDelay,
): Unit
```

## Saved state (package: `pro.respawn.flowmvi.savedstate.plugins`)

Persist and restore state automatically. Install early to avoid being overwritten by later `onStart` plugins.

```kotlin
package pro.respawn.flowmvi.savedstate.plugins

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> saveStatePlugin(
    saver: Saver<S>,
    context: CoroutineContext = EmptyCoroutineContext,
    name: String? = PluginNameSuffix,
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    resetOnException: Boolean = true,
): LazyPlugin<S, I, A>

@FlowMVIDSL
public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.saveState(
    saver: Saver<S>,
    context: CoroutineContext = EmptyCoroutineContext,
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    name: String? = PluginNameSuffix,
    resetOnException: Boolean = true,
): Unit
```

```kotlin
package pro.respawn.flowmvi.savedstate.plugins

@FlowMVIDSL
public inline fun <reified T : S, reified S : MVIState, I : MVIIntent, A : MVIAction> serializeStatePlugin(
    noinline path: suspend () -> String,
    serializer: KSerializer<T>,
    json: Json = DefaultJson,
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    name: String? = serializer.descriptor.serialName.plus(PluginNameSuffix),
    context: CoroutineContext = Dispatchers.Default,
    resetOnException: Boolean = true,
    noinline recover: suspend (Exception) -> T? = ThrowRecover,
): LazyPlugin<S, I, A>

@FlowMVIDSL
public inline fun <reified T : S, reified S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.serializeState(
    noinline path: suspend () -> String,
    serializer: KSerializer<T>,
    json: Json = DefaultJson,
    name: String? = "${serializer.descriptor.serialName}$PluginNameSuffix",
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    context: CoroutineContext = Dispatchers.Default,
    resetOnException: Boolean = true,
    noinline recover: suspend (Exception) -> T? = ThrowRecover,
): Unit
```

```kotlin
package pro.respawn.flowmvi.savedstate.plugins

@FlowMVIDSL
public inline fun <reified T, reified S : MVIState, I : MVIIntent, A : MVIAction> parcelizeStatePlugin(
    handle: SavedStateHandle,
    context: CoroutineContext = Dispatchers.IO,
    key: String = key<T>(),
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    resetOnException: Boolean = true,
    name: String? = "$key$PluginNameSuffix",
    noinline recover: suspend (Exception) -> T? = ThrowRecover,
): LazyPlugin<S, I, A> where T : Parcelable, T : S

@FlowMVIDSL
public inline fun <reified T, reified S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.parcelizeState(
    handle: SavedStateHandle,
    context: CoroutineContext = Dispatchers.IO,
    key: String = key<T>(),
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    name: String? = "$key$PluginNameSuffix",
    resetOnException: Boolean = true,
    noinline recover: suspend (Exception) -> T? = ThrowRecover,
): Unit where T : Parcelable, T : S
```

## Essenty integration (package: `pro.respawn.flowmvi.essenty.plugins`)

Keeps state with Essenty StateKeeper (Decompose). Use in Essenty-based apps.

```kotlin
package pro.respawn.flowmvi.essenty.plugins

@FlowMVIDSL
public inline fun <reified T : S, S : MVIState, I : MVIIntent, A : MVIAction> keepStatePlugin(
    keeper: StateKeeper,
    serializer: KSerializer<T>,
    key: String = serializer.descriptor.serialName,
    name: String? = "${key}KeepStatePlugin",
): StorePlugin<S, I, A>

@FlowMVIDSL
public inline fun <reified T : S, S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.keepState(
    keeper: StateKeeper,
    serializer: KSerializer<T>,
    key: String = serializer.descriptor.serialName,
    name: String? = "${key}KeepStatePlugin",
): Unit
```

