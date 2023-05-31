package pro.respawn.flowmvi.base

import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.dsl.FlowMVIDSL

@FlowMVIDSL
public class StoreBuilder<S : MVIState, I : MVIIntent> internal constructor() {

    public var parallelIntents: Boolean = false
    private var plugins: MutableList<StorePlugin<S, I>> = mutableListOf()

    public fun install(plugin: StorePlugin<S, I>) {
        plugins.add(plugin)
    }

    public fun install(
        plugin: StorePluginBuilder<S, I>.() -> Unit
    ): Unit = install(storePlugin(plugin))

    internal fun configuration(reducer: Reducer<S, I>): StoreConfiguration<S, I> = StoreConfiguration(
        parallelIntents = parallelIntents,
        reducer = reducer,
        plugins = plugins.toList(),
    )
}

public fun <S : MVIState, I : MVIIntent> store(
    configure: StoreBuilder<S, I>.() -> Reducer<S, I>,
): StoreConfiguration<S, I> = StoreBuilder<S, I>().run {
    configuration(configure())
}
