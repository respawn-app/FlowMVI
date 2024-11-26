package pro.respawn.flowmvi.decorator

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.util.setOnce

@FlowMVIDSL
public class DecoratorBuilder<S : MVIState, I : MVIIntent, A : MVIAction> {

    private var _onIntent: DecorateValue<S, I, A, I>? = null
    private var _onState: DecorateState<S, I, A>? = null
    private var _onAction: DecorateValue<S, I, A, A>? = null
    private var _onException: DecorateValue<S, I, A, Exception>? = null
    private var _onStart: Decorate<S, I, A>? = null
    private var _onSubscribe: DecorateArg<S, I, A, Int>? = null
    private var _onUnsubscribe: DecorateArg<S, I, A, Int>? = null

    public var name: String? = null

    @FlowMVIDSL
    public fun onIntent(block: DecorateValue<S, I, A, I>): Unit = setOnce(::_onIntent, block)

    @FlowMVIDSL
    public fun onState(block: DecorateState<S, I, A>): Unit = setOnce(::_onState, block)

    @FlowMVIDSL
    public fun onAction(block: DecorateValue<S, I, A, A>): Unit = setOnce(::_onAction, block)

    @FlowMVIDSL
    public fun onException(block: DecorateValue<S, I, A, Exception>): Unit = setOnce(::_onException, block)

    @FlowMVIDSL
    public fun onStart(block: Decorate<S, I, A>): Unit = setOnce(::_onStart, block)

    @FlowMVIDSL
    public fun onSubscribe(block: DecorateArg<S, I, A, Int>): Unit = setOnce(::_onSubscribe, block)

    @FlowMVIDSL
    public fun onUnsubscribe(block: DecorateArg<S, I, A, Int>): Unit = setOnce(::_onUnsubscribe, block)

    @PublishedApi
    internal fun build(): PluginDecorator<S, I, A> = PluginDecorator(
        name = name,
        onIntent = _onIntent,
        onState = _onState,
        onAction = _onAction,
        onException = _onException,
        onStart = _onStart,
        onSubscribe = _onSubscribe,
        onUnsubscribe = _onUnsubscribe
    )
}
