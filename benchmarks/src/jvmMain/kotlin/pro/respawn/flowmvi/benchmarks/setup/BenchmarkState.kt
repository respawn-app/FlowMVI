package pro.respawn.flowmvi.benchmarks.setup

import pro.respawn.flowmvi.api.MVIState

internal data class BenchmarkState(
    val counter: Int = 0
) : MVIState
