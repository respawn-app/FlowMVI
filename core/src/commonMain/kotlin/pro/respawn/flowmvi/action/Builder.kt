package pro.respawn.flowmvi.action

import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.dsl.FlowMVIDSL

@FlowMVIDSL
public class StoreBuilder<S : MVIState, I : MVIIntent, A : MVIAction> internal constructor() {

    public var parallelIntents: Boolean = false
    public var actionShareBehavior: ActionShareBehavior = ActionShareBehavior.Distribute()
    private var plugins: MutableList<StorePlugin<S, I, A>> = mutableListOf()

    public fun install(plugin: StorePlugin<S, I, A>) {
        plugins.add(plugin)
    }

    public fun install(
        plugin: StorePluginBuilder<S, I, A>.() -> Unit
    ): Unit = install(storePlugin(plugin))

    internal fun configuration(
        reducer: Reducer<S, I, A>
    ): StoreConfiguration<S, I, A> = StoreConfiguration(
        parallelIntents = parallelIntents,
        actionShareBehavior = actionShareBehavior,
        plugins = plugins,
        reducer = reducer,
    )
}

public fun <S : MVIState, I : MVIIntent, A : MVIAction> actionStore(
    configure: StoreBuilder<S, I, A>.() -> Reducer<S, I, A>,
): StoreConfiguration<S, I, A> = StoreBuilder<S, I, A>().run {
    configuration(configure())
}
