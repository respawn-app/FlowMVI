package pro.respawn.flowmvi.benchmarks.setup

import pro.respawn.flowmvi.api.MVIIntent

internal sealed interface BenchmarkIntent : MVIIntent {
    data object Increment : BenchmarkIntent
}
