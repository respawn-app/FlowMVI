package pro.respawn.flowmvi.metrics

import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.scopes.TerminalScope
import io.kotest.core.test.TestCase
import io.kotest.engine.coroutines.CoroutineDispatcherFactory
import io.kotest.engine.coroutines.coroutineTestScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.decorator.decorates
import pro.respawn.flowmvi.dsl.StoreConfigurationBuilder
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.NoOpPlugin
import pro.respawn.flowmvi.plugins.TimeTravel
import pro.respawn.flowmvi.plugins.timeTravelPlugin
import pro.respawn.flowmvi.test.TestStore
import pro.respawn.flowmvi.test.plugin.PluginTestScope
import pro.respawn.flowmvi.test.plugin.test
import pro.respawn.flowmvi.test.test
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal data class TestState(val value: Int = 0) : MVIState
internal data class TestIntent(val id: Int = 0) : MVIIntent
internal data class TestAction(val id: Int = 0) : MVIAction

internal typealias TestCtx = PluginTestScope<TestState, TestIntent, TestAction>
internal typealias TestMetrics = MetricsCollector<TestState, TestIntent, TestAction>

internal class MutableTimeSource(initial: Duration = ZERO) : TimeSource {

    private var current: Duration = initial

    override fun markNow(): TimeMark = object : TimeMark {
        private val origin = current
        override fun elapsedNow(): Duration = current - origin
    }

    fun advanceBy(duration: Duration) {
        current += duration
    }
}

internal class MutableClock(initial: Instant) : Clock {

    private var current = initial

    override fun now(): Instant = current

    fun advanceBy(duration: Duration) {
        current += duration
    }
}

internal suspend inline fun TerminalScope.testCollector(
    windowSeconds: Int = 60,
    emaAlpha: Double = 0.5,
    bucketDuration: Duration = 1.seconds,
    timeTravel: TimeTravel<TestState, TestIntent, TestAction> = TimeTravel(),
    child: StorePlugin<TestState, TestIntent, TestAction> = NoOpPlugin(),
    noinline configuration: StoreConfigurationBuilder.() -> Unit = { debuggable = true },
    crossinline block: suspend TestCtx.(TestMetrics) -> Unit,
) {
    val clock = MutableClock(Instant.fromEpochMilliseconds(0))
    val ts = MutableTimeSource()
    val collector = MetricsCollector<TestState, TestIntent, TestAction>(
        reportingScope = coroutineTestScope,
        offloadContext = EmptyCoroutineContext,
        bucketDuration = bucketDuration,
        windowSeconds = windowSeconds,
        emaAlpha = emaAlpha,
        clock = clock,
        timeSource = ts
    )
    val wrapped = collector.asDecorator(null) decorates child
    wrapped.test(TestState(), timeTravel, configuration) {
        block(collector)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
internal fun Spec.asUnconfined() {
    coroutineTestScope = true
    coroutineDispatcherFactory = object : CoroutineDispatcherFactory {
        val dispatcher = UnconfinedTestDispatcher()
        override suspend fun <T> withDispatcher(spec: Spec, f: suspend () -> T): T = withContext(dispatcher) { f() }

        override suspend fun <T> withDispatcher(
            testCase: TestCase, f: suspend () -> T
        ): T = withContext(dispatcher) { f() }
    }
    this.coroutineDebugProbes = true
}

@OptIn(ExperimentalFlowMVIAPI::class)
internal suspend inline fun TerminalScope.testCollectorAsStore(
    windowSeconds: Int = 60,
    emaAlpha: Double = 0.5,
    bucketDuration: Duration = 1.seconds,
    timeTravel: TimeTravel<TestState, TestIntent, TestAction> = TimeTravel(),
    noinline configuration: StoreConfigurationBuilder.() -> Unit = { debuggable = true },
    crossinline block: suspend TestStore<TestState, TestIntent, TestAction>.(TestMetrics) -> Unit,
) {
    val clock = MutableClock(Instant.fromEpochMilliseconds(0))
    val ts = MutableTimeSource()
    val collector = MetricsCollector<TestState, TestIntent, TestAction>(
        reportingScope = coroutineTestScope,
        offloadContext = EmptyCoroutineContext,
        bucketDuration = bucketDuration,
        windowSeconds = windowSeconds,
        emaAlpha = emaAlpha,
        clock = clock,
        timeSource = ts
    )
    store(TestState()) {
        configure(configuration)
        install(timeTravelPlugin(timeTravel))
        install(collector.asDecorator(null))
    }.test { block.invoke(this, collector) }
}
