package pro.respawn.flowmvi

import io.kotest.common.ExperimentalKotest
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalKotest::class)
class CoreTestConfig : AbstractProjectConfig() {

    override val coroutineTestScope = true
    override val failOnEmptyTestSuite: Boolean = true
    override val isolationMode: IsolationMode = IsolationMode.SingleInstance
    override val timeout: Duration = 3.seconds
    override val coroutineDebugProbes: Boolean = true
    override val invocationTimeout = 5000.milliseconds
}
