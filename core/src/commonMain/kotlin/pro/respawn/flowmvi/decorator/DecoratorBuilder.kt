package pro.respawn.flowmvi.decorator

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.impl.decorator.DecoratorInstance
import pro.respawn.flowmvi.util.setOnce

public class DecoratorBuilder<S : MVIState, I : MVIIntent, A : MVIAction> {

    private var _onIntent: (suspend DecoratorContext<S, I, A, I>.(I) -> I?)? = null
    private var _onState: (suspend DecoratorContext<S, I, A, S>.(S, S) -> S?)? = null
    private var _onAction: (suspend DecoratorContext<S, I, A, A>.(A) -> A?)? = null
    private var _onException: (suspend DecoratorContext<S, I, A, Exception>.(Exception) -> Exception?)? = null
    private var _onStart: (suspend DecoratorContext<S, I, A, Unit>.() -> Unit)? = null
    private var _onSubscribe: (suspend DecoratorContext<S, I, A, Int>.(subs: Int) -> Unit)? = null
    private var _onUnsubscribe: (suspend DecoratorContext<S, I, A, Int>.(subs: Int) -> Unit)? = null

    public var name: String? = null

    @FlowMVIDSL
    public fun onIntent(
        block: suspend DecoratorContext<S, I, A, I>.(intent: I) -> I?
    ): Unit = setOnce(::_onIntent, block)

    @FlowMVIDSL
    public fun onState(
        block: suspend DecoratorContext<S, I, A, S>.(old: S, new: S) -> S?
    ): Unit = setOnce(::_onState, block)

    @FlowMVIDSL
    public fun onAction(
        block: suspend DecoratorContext<S, I, A, A>.(action: A) -> A?
    ): Unit = setOnce(::_onAction, block)

    @FlowMVIDSL
    public fun onException(
        block: suspend DecoratorContext<S, I, A, Exception>.(e: Exception) -> Exception?
    ): Unit = setOnce(::_onException, block)

    @FlowMVIDSL
    public fun onStart(
        block: suspend DecoratorContext<S, I, A, Unit>.() -> Unit
    ): Unit = setOnce(::_onStart, block)

    @FlowMVIDSL
    public fun onSubscribe(
        block: suspend DecoratorContext<S, I, A, Int>.(subs: Int) -> Unit
    ): Unit = setOnce(::_onSubscribe, block)

    @FlowMVIDSL
    public fun onUnsubscribe(
        block: suspend DecoratorContext<S, I, A, Int>.(subs: Int) -> Unit
    ): Unit = setOnce(::_onUnsubscribe, block)

    @PublishedApi
    internal fun build(): DecoratorInstance<S, I, A> = DecoratorInstance(
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
