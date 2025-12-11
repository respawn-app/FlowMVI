package pro.respawn.flowmvi.metrics

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.test.plugin.PluginTestScope
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Instant
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal data class TestState(val value: Int = 0) : MVIState
internal data class TestIntent(val id: Int = 0) : MVIIntent
internal data class TestAction(val id: Int = 0) : MVIAction

internal typealias TestCtx = PluginTestScope<TestState, TestIntent, TestAction>

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
