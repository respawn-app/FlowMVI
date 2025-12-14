package pro.respawn.flowmvi.benchmarks.setup.mvikotlin

import com.arkivanov.mvikotlin.core.store.create
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent.Increment
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkState

internal fun mviKotlinCounterStore() = DefaultStoreFactory().create<BenchmarkIntent, BenchmarkState>(
    name = "benchmark-counter",
    autoInit = false,
    initialState = BenchmarkState(),
) { intent ->
    when (intent) {
        is Increment -> copy(counter = counter + 1)
    }
}
