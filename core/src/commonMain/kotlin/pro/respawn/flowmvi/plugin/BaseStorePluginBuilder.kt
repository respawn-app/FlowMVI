package pro.respawn.flowmvi.plugin

import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.dsl.FlowMVIDSL

@FlowMVIDSL
public open class BaseStorePluginBuilder<S : MVIState, I : MVIIntent, C> internal constructor() {

    protected var _intent: C.(I) -> I? = { it }
        private set
    protected var _state: C.(S) -> S? = { it }
        private set
    protected var _start: C.() -> Unit = { }
        private set
    protected var _exception: C.(e: Exception) -> Exception? = { it }
        private set
    protected var _stop: () -> Unit = { }
        private set
    public var name: String? = null

    public fun onIntent(block: C.(intent: I) -> I?) {
        _intent = block
    }

    public fun onState(@BuilderInference block: C.(state: S) -> S?) {
        _state = block
    }

    public fun onStart(@BuilderInference block: C.() -> Unit) {
        _start = block
    }

    public fun onStop(block: () -> Unit) {
        _stop = block
    }

    public fun onException(block: C.(e: Exception) -> Exception?) {
        _exception = block
    }
}
