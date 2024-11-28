package pro.respawn.flowmvi.benchmarks.setup

import pro.respawn.flowmvi.api.MVIState

data class BenchmarkState(
    val counter: Int = 0
) : MVIState
