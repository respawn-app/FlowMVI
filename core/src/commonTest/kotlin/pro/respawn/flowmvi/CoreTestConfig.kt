package pro.respawn.flowmvi

import io.kotest.common.ExperimentalKotest
import io.kotest.core.config.AbstractProjectConfig

class CoreTestConfig : AbstractProjectConfig() {

    @OptIn(ExperimentalKotest::class)
    override var testCoroutineDispatcher = true

    override val coroutineDebugProbes: Boolean = true
    override val invocationTimeout = 5000L
    override val parallelism: Int = 1
    override val coroutineTestScope = true
}
