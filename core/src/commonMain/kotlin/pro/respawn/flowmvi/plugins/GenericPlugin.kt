package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.storePlugin

public class GenericPluginBuilder internal constructor() {

    private var intent: suspend (MVIIntent) -> Unit = {}
    private var state: suspend (old: MVIState, new: MVIState) -> Unit = { _, _ -> }
    private var action: suspend (MVIAction) -> Unit = {}
    private var exception: suspend (e: Exception) -> Unit = {}
    private var start: suspend () -> Unit = {}
    private var subscribe: suspend (subscriptionCount: Int) -> Unit = {}
    private var stop: (e: Exception?) -> Unit = {}
    public var name: String? = null

    @FlowMVIDSL
    public fun onIntent(block: suspend (intent: MVIIntent) -> Unit) {
        intent = block
    }

    @FlowMVIDSL
    public fun onState(block: suspend (old: MVIState, new: MVIState) -> Unit) {
        state = block
    }

    @FlowMVIDSL
    public fun onStart(block: suspend () -> Unit) {
        start = block
    }

    @FlowMVIDSL
    public fun onStop(block: (e: Exception?) -> Unit) {
        stop = block
    }

    @FlowMVIDSL
    public fun onException(block: suspend (e: Exception) -> Unit) {
        exception = block
    }

    @FlowMVIDSL
    public fun onAction(block: suspend (action: MVIAction) -> Unit) {
        action = block
    }

    @FlowMVIDSL
    public fun onSubscribe(block: suspend (subscriptionCount: Int) -> Unit) {
        subscribe = block
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <S : MVIState, I : MVIIntent, A : MVIAction> build() = storePlugin {
        name = this@GenericPluginBuilder.name
        onIntent {
            this@GenericPluginBuilder.intent(it)
            it
        }
        onAction {
            this@GenericPluginBuilder.action(it)
            it
        }
        onState { old, new ->
            state(old, new)
            new
        }
        onException {
            exception(it)
            it
        }
        onSubscribe(subscribe)
        onStart(start)
        onStop(stop)
        // we can safely cast as this plugin can't affect the store in any way
    } as StorePlugin<S, I, A>
}

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> genericPlugin(
    @BuilderInference builder: GenericPluginBuilder.() -> Unit,
): StorePlugin<S, I, A> = GenericPluginBuilder().apply(builder).build()
