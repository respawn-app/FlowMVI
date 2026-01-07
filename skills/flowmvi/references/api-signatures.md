# FlowMVI API Signatures (from source)

Use this file when you need exact, compiling signatures for core FlowMVI APIs.

## Store builders

```kotlin
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> store(
    initial: S,
    @BuilderInference configure: BuildStore<S, I, A>,
): Store<S, I, A>

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> store(
    initial: S,
    scope: CoroutineScope,
    @BuilderInference configure: BuildStore<S, I, A>,
): Store<S, I, A>

@FlowMVIDSL
@JvmName("noActionStore")
public inline fun <S : MVIState, I : MVIIntent> store(
    initial: S,
    @BuilderInference configure: BuildStore<S, I, Nothing>,
): Store<S, I, Nothing>

@FlowMVIDSL
@JvmName("noActionStore")
public inline fun <S : MVIState, I : MVIIntent> store(
    initial: S,
    scope: CoroutineScope,
    @BuilderInference configure: BuildStore<S, I, Nothing>,
): Store<S, I, Nothing>

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> lazyStore(
    initial: S,
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    @BuilderInference crossinline configure: BuildStore<S, I, A>,
): Lazy<Store<S, I, A>>

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> lazyStore(
    initial: S,
    scope: CoroutineScope,
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    @BuilderInference crossinline configure: BuildStore<S, I, A>,
): Lazy<Store<S, I, A>>
```

## Store builder and configuration

```kotlin
public typealias BuildStore<S, I, A> = StoreBuilder<S, I, A>.() -> Unit

@FlowMVIDSL
public class StoreBuilder<S : MVIState, I : MVIIntent, A : MVIAction> internal constructor(
    public val initial: S,
) {
    @FlowMVIDSL
    public inline fun configure(block: StoreConfigurationBuilder.() -> Unit): Unit

    @FlowMVIDSL
    public infix fun install(plugins: Iterable<LazyPlugin<S, I, A>>): Unit

    @FlowMVIDSL
    public fun install(
        plugin: LazyPlugin<S, I, A>,
        vararg other: LazyPlugin<S, I, A>,
    ): Unit

    @FlowMVIDSL
    public inline infix fun install(
        crossinline block: LazyPluginBuilder<S, I, A>.() -> Unit
    ): Unit

    @FlowMVIDSL
    public fun LazyPlugin<S, I, A>.install(): Unit

    @FlowMVIDSL
    @ExperimentalFlowMVIAPI
    public infix fun install(decorators: Iterable<PluginDecorator<S, I, A>>): Unit

    @FlowMVIDSL
    @ExperimentalFlowMVIAPI
    public fun install(decorator: PluginDecorator<S, I, A>, vararg other: PluginDecorator<S, I, A>)

    @FlowMVIDSL
    @ExperimentalFlowMVIAPI
    public inline infix fun decorate(block: DecoratorBuilder<S, I, A>.() -> Unit): Unit

    @FlowMVIDSL
    @ExperimentalFlowMVIAPI
    public fun PluginDecorator<S, I, A>.install(): Unit
}

@FlowMVIDSL
public class StoreConfigurationBuilder internal constructor() {
    @FlowMVIDSL public var parallelIntents: Boolean
    @FlowMVIDSL public var actionShareBehavior: ActionShareBehavior
    @FlowMVIDSL public var stateStrategy: StateStrategy
    @FlowMVIDSL public var intentCapacity: Int
    @FlowMVIDSL public var onOverflow: BufferOverflow
    @FlowMVIDSL public var debuggable: Boolean
    @FlowMVIDSL public var allowIdleSubscriptions: Boolean?
    @FlowMVIDSL public var allowTransientSubscriptions: Boolean?
    @FlowMVIDSL public var coroutineContext: CoroutineContext
    @FlowMVIDSL public var logger: StoreLogger?
    @FlowMVIDSL public var verifyPlugins: Boolean?
    @FlowMVIDSL public var name: String?
}

@FlowMVIDSL
public inline fun <S : MVIState> configuration(
    initial: S,
    block: StoreConfigurationBuilder.() -> Unit,
): StoreConfiguration<S>
```

## Store lifecycle and subscription

```kotlin
public interface ImmutableStoreLifecycle {
    public suspend fun awaitStartup()
    public suspend fun awaitUntilClosed()
    public val isActive: Boolean
    public val isStarted: Boolean
}

public interface StoreLifecycle : ImmutableStoreLifecycle, AutoCloseable {
    public suspend fun closeAndWait()
}

public interface ImmutableStore<out S : MVIState, in I : MVIIntent, out A : MVIAction> :
    ImmutableStoreLifecycle,
    StateProvider<S> {
    public val name: String?
    public fun start(scope: CoroutineScope): ImmutableStoreLifecycle
    public fun CoroutineScope.subscribe(block: suspend Provider<S, I, A>.() -> Unit): Job
}

public interface Store<out S : MVIState, in I : MVIIntent, out A : MVIAction> :
    ImmutableStore<S, I, A>,
    IntentReceiver<I>,
    StoreLifecycle {
    override fun start(scope: CoroutineScope): StoreLifecycle
}
```

### Subscribe helpers (DSL)

```kotlin
@FlowMVIDSL
public suspend inline fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableStore<S, I, A>.collect(
    @BuilderInference crossinline consume: suspend Provider<S, I, A>.() -> Unit,
): Unit

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> CoroutineScope.subscribe(
    store: ImmutableStore<S, I, A>,
    @BuilderInference crossinline consume: suspend Provider<S, I, A>.() -> Unit,
): Job

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> CoroutineScope.subscribe(
    store: ImmutableStore<S, I, A>,
    crossinline consume: suspend (action: A) -> Unit,
    crossinline render: suspend (state: S) -> Unit,
): Job

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> CoroutineScope.subscribe(
    store: ImmutableStore<S, I, A>,
    crossinline render: suspend (state: S) -> Unit,
): Job

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.subscribe(
    store: ImmutableStore<S, I, A>,
    scope: CoroutineScope
): Job where T : ActionConsumer<A>, T : StateConsumer<S>

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StateConsumer<S>.subscribe(
    store: ImmutableStore<S, I, A>,
    scope: CoroutineScope
): Job
```

## PipelineContext and receivers

```kotlin
@FlowMVIDSL
public interface PipelineContext<S : MVIState, I : MVIIntent, A : MVIAction> :
    IntentReceiver<I>,
    StateReceiver<S>,
    ActionReceiver<A>,
    CoroutineScope,
    StoreLifecycle,
    SubscriptionAware,
    CoroutineContext.Element {

    public val config: StoreConfiguration<S>

    public suspend fun <T> Flow<T>.consume(
        context: CoroutineContext = EmptyCoroutineContext
    ): Unit

    public companion object : CoroutineContext.Key<PipelineContext<*, *, *>>
    override val key: CoroutineContext.Key<*>
}

public interface IntentReceiver<in I : MVIIntent> {
    public suspend fun emit(intent: I)
    public fun intent(intent: I)
}

public interface ActionReceiver<in A : MVIAction> {
    public fun send(action: A)
    public suspend fun action(action: A)
}

public interface StateReceiver<S : MVIState> : ImmediateStateReceiver<S> {
    public suspend fun updateState(transform: suspend S.() -> S)
    public suspend fun withState(block: suspend S.() -> Unit)
}

public interface ImmediateStateReceiver<S : MVIState> : StateProvider<S> {
    public fun compareAndSet(old: S, new: S): Boolean
    override val states: StateFlow<S>
}
```

### State DSL helpers

```kotlin
@DelicateStoreApi
public inline val <S : MVIState> StateProvider<S>.state: S

@FlowMVIDSL
public inline fun <S : MVIState> ImmediateStateReceiver<S>.updateStateImmediate(
    @BuilderInference transform: S.() -> S
)

@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState> StateReceiver<S>.withState(
    @BuilderInference crossinline block: suspend T.() -> Unit
): Unit

@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState> StateReceiver<S>.updateState(
    @BuilderInference crossinline transform: suspend T.() -> S
): Unit

@FlowMVIDSL
@JvmName("updateStateImmediateTyped")
public inline fun <reified T : S, S : MVIState> ImmediateStateReceiver<S>.updateStateImmediate(
    @BuilderInference transform: T.() -> S
): Unit

@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState> PipelineContext<S, *, *>.withStateOrThrow(
    @BuilderInference crossinline block: suspend T.() -> Unit
): Unit

@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState> PipelineContext<S, *, *>.updateStateOrThrow(
    @BuilderInference crossinline transform: suspend T.() -> S
): Unit
```

## Lambda intents (MVVM+)

```kotlin
@JvmInline
public value class LambdaIntent<S : MVIState, A : MVIAction>(
    private val block: suspend PipelineContext<S, LambdaIntent<S, A>, A>.() -> Unit
) : MVIIntent

@FlowMVIDSL
public fun <S : MVIState, A : MVIAction> IntentReceiver<LambdaIntent<S, A>>.send(
    @BuilderInference block: suspend PipelineContext<S, LambdaIntent<S, A>, A>.() -> Unit
): Unit

@FlowMVIDSL
public fun <S : MVIState, A : MVIAction> IntentReceiver<LambdaIntent<S, A>>.intent(
    @BuilderInference block: suspend PipelineContext<S, LambdaIntent<S, A>, A>.() -> Unit
): Unit

@FlowMVIDSL
public suspend fun <S : MVIState, A : MVIAction> IntentReceiver<LambdaIntent<S, A>>.emit(
    @BuilderInference block: suspend PipelineContext<S, LambdaIntent<S, A>, A>.() -> Unit
): Unit

@FlowMVIDSL
public fun <S : MVIState, A : MVIAction> StoreBuilder<S, LambdaIntent<S, A>, A>.reduceLambdas(
    name: String = ReducePluginName,
    consume: Boolean = true,
): Unit
```

## State strategy and action sharing

```kotlin
public sealed interface StateStrategy {
    public object Immediate : StateStrategy
    public data class Atomic(val reentrant: Boolean = true) : StateStrategy
}

public sealed interface ActionShareBehavior {
    public data class Share(
        val buffer: Int = DefaultBufferSize,
        val replay: Int = 0,
        val overflow: BufferOverflow = BufferOverflow.SUSPEND,
    ) : ActionShareBehavior

    public data class Distribute(
        val buffer: Int = DefaultBufferSize,
        val overflow: BufferOverflow = BufferOverflow.SUSPEND
    ) : ActionShareBehavior

    public data class Restrict(
        val buffer: Int = DefaultBufferSize,
        val overflow: BufferOverflow = BufferOverflow.SUSPEND
    ) : ActionShareBehavior

    public data object Disabled : ActionShareBehavior
}
```

## Container helpers

```kotlin
public interface ImmutableContainer<S : MVIState, I : MVIIntent, A : MVIAction> {
    public val store: ImmutableStore<S, I, A>
}

public interface Container<S : MVIState, I : MVIIntent, A : MVIAction> : ImmutableContainer<S, I, A> {
    override val store: Store<S, I, A>
}
```
