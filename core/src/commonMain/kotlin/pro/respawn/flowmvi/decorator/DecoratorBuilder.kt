package pro.respawn.flowmvi.decorator

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.plugins.compositePlugin
import pro.respawn.flowmvi.util.setOnce

/**
 * This is a builder object for [PluginDecorator]. It wraps another [StorePlugin]
 * and implements the [StorePlugin] interface.
 *
 * Decorators can wrap an individual plugin (using [PluginDecorator.decorates]), in which case they are pretty much
 * normal decorators and you can access the underlying plugin using the parameter of a method.
 *
 * However, if a decorator is installed over the entire store (in the [StoreBuilder]), it
 * will wrap **ALL** of the store's plugins, expressed as a single [compositePlugin].
 *
 * Decorators are very similar to plugins, but they **differ** in these ways:
 *  * Decorators are installed **after** all of the plugins in a store. It means, that if you install a decorator to
 *  the [StoreBuilder], it will wrap **all** plugins of a store.
 *  * Decorators are installed in the order they are declared. If a decorator is installed after another, it will **wrap
 *  all previous decorators** with itself, which in turn wrap all plugins.
 *  * Decorators can be used to manipulate the plugin chain in more ways than plugins. For example, unlike plugins, if
 *  you do not invoke the appropriate method of the child plugin manually, it will skip the **entire chain**.
 *  * Values you return from decorator methods are not the "next" values like with plugins, but should be treated as
 *  the **final** values, i.e, if you return a state, it will be the state that the store will have,
 *  unless this decorator is decorated by another one
 *
 *  ### Warning:
 *  Decorators are experimental and currently are waiting for context parameter support to enable the correct
 *  DSL. The current DSL is temporary, and as such, there WILL be at least one breaking change before these go stable,
 *  it may even be a behavioral one. If you use these, monitor the release notes for changes
 *  in the API and, if possible, stick to built-in decorators.**
 *
 *  Builder methods will throw [IllegalArgumentException] if they are assigned multiple times.
 *  Each plugin can only have **one** block per each type of [StorePlugin] callback.
 */
@Suppress("TooManyFunctions")
@FlowMVIDSL
public class DecoratorBuilder<S : MVIState, I : MVIIntent, A : MVIAction> {

    private var _onIntent: DecorateValue<S, I, A, I>? = null
    private var _onState: DecorateState<S, I, A>? = null
    private var _onAction: DecorateValue<S, I, A, A>? = null
    private var _onException: DecorateValue<S, I, A, Exception>? = null
    private var _onStart: Decorate<S, I, A>? = null
    private var _onSubscribe: DecorateArg<S, I, A, Int>? = null
    private var _onUnsubscribe: DecorateArg<S, I, A, Int>? = null
    private var _onStop: DecorateOnStop<S, I, A>? = null
    private var _onUndeliveredIntent: DecorateUndelivered<S, I, A, I>? = null
    private var _onUndeliveredAction: DecorateUndelivered<S, I, A, A>? = null

    /**
     * The name of the decorator. Must be unique if defined. The same rules apply as for the [StorePlugin.name] property
     */
    public var name: String? = null

    /**
     * Wraps the [StorePlugin.onStart] method of the child plugin passed in the [block] parameter.
     *
     *
     * The child's [StorePlugin.onStart] method will **not** be invoked
     * unless you manually call [StorePlugin.onStart] inside this block!
     *
     * For pre-context parameters version, to correctly call the child method, use `child.run { onStart() }`
     *
     * See the [DecoratorBuilder] documentation for details on how this function behaves.
     */
    @FlowMVIDSL
    public fun onStart(block: Decorate<S, I, A>): Unit = setOnce(::_onStart, block)

    /**
     * Wraps the [StorePlugin.onIntent] method of the child plugin passed in the [block] parameter.
     *
     * The [StorePlugin.onIntent] method will **not** be invoked
     * unless you manually call [StorePlugin.onIntent] inside this block!
     *
     * For pre-context parameters version, to correctly call the child method, use `child.run { onIntent(intent) }`
     *
     * The return value of [block] will mean what the store reports as the result of handling the intent.
     * Use `null` to consider the intent handled. Use the return value of `child.onIntent` to delegate the result.
     *
     * See the [DecoratorBuilder] documentation for details on how this function behaves.
     */
    @FlowMVIDSL
    public fun onIntent(block: DecorateValue<S, I, A, I>): Unit = setOnce(::_onIntent, block)

    /**
     * Wraps the [StorePlugin.onState] method of the child plugin passed in the [block] parameter.
     *
     * The child's [StorePlugin.onState] method will **not** be invoked
     * unless you manually call [StorePlugin.onState] inside this block.
     *
     * For pre-context parameters version, to correctly call the child method, use `child.run { onState(old, new) }`
     *
     * The return value of [block] will mean what the **state will be set to**, i.e. the end value.
     * Use `null` to not change the state. Use the return value of `child.onState` to delegate the result.
     *
     * See the [DecoratorBuilder] documentation for details on how this function behaves.
     */
    @FlowMVIDSL
    public fun onState(block: DecorateState<S, I, A>): Unit = setOnce(::_onState, block)

    /**
     * Wraps the [StorePlugin.onAction] method of the child plugin passed in the [block] parameter.
     *
     * The child [StorePlugin.onAction] method will **not** be invoked
     * unless you manually call [StorePlugin.onAction] inside this block.
     *
     * For pre-context parameters version, to correctly call the child method, use `child.run { onAction(action) }`
     *
     * The return value of [block] will mean what action will be actually send to subscribers, i.e. the end result.
     * Use `null` to not send anything. Use the return value of `child.onAction` to delegate the result.
     *
     * See the [DecoratorBuilder] documentation for details on how this function behaves.
     */
    @FlowMVIDSL
    public fun onAction(block: DecorateValue<S, I, A, A>): Unit = setOnce(::_onAction, block)

    /**
     * Wraps the [StorePlugin.onException] method of the child plugin passed in the [block] parameter.
     *
     * The plugin's [StorePlugin.onException] method will **not** be invoked,
     * unless you manually call [StorePlugin.onException] inside this block!
     *
     * For pre-context parameters version, to correctly call the child method, use `child.run { onException(e) }`
     *
     * The return value of [block] will mean what the store reports as the result of handling the exception.
     * Use `null` to swallow the exception. Use the return value of `child.onException` to delegate the result.
     *
     * See the [DecoratorBuilder] documentation for details on how this function behaves.
     */
    @FlowMVIDSL
    public fun onException(block: DecorateValue<S, I, A, Exception>): Unit = setOnce(::_onException, block)

    /**
     * Wraps the [StorePlugin.onSubscribe] method of the child plugin passed in the [block] parameter.
     *
     * The child plugin will not know an event has occurred unless
     * you call the [StorePlugin.onSubscribe] method inside this block manually!
     *
     * For pre-context parameters version, to correctly call the child method, use `child.run { onSubscribe(e) }`
     *
     * See the [DecoratorBuilder] documentation for details on how this function behaves.
     */
    @FlowMVIDSL
    public fun onSubscribe(block: DecorateArg<S, I, A, Int>): Unit = setOnce(::_onSubscribe, block)

    /**
     * Wraps the [StorePlugin.onUnsubscribe] method of the child plugin passed in the [block] parameter.
     *
     * The child plugin will not know an event has occurred unless
     * you call the [StorePlugin.onUnsubscribe] method inside this block manually!
     *
     * For pre-context parameters version, to correctly call the child method, use `child.run { onUnsubscribe(e) }`
     *
     * See the [DecoratorBuilder] documentation for details on how this function behaves.
     */
    @FlowMVIDSL
    public fun onUnsubscribe(block: DecorateArg<S, I, A, Int>): Unit = setOnce(::_onUnsubscribe, block)

    /**
     * Wraps the [StorePlugin.onStop] method of the child plugin passed in the [block] parameter.
     *
     * The child plugin will not know the store has stopped unless
     * you call the [StorePlugin.onStop] method inside this block manually. Be careful of resources not being properly
     * cleaned up!
     *
     * For pre-context parameters version, to correctly call the child method, use `child.run { onStop(e) }`
     *
     * See the [DecoratorBuilder] documentation for details on how this function behaves.
     */
    @FlowMVIDSL
    public fun onStop(block: DecorateOnStop<S, I, A>): Unit = setOnce(::_onStop, block)

    /**
     * Wraps the [StorePlugin.onUndeliveredIntent] method of the child plugin passed in the [block] parameter.
     *
     * The child plugin will not know an event has occurred unless
     * you call the [StorePlugin.onUndeliveredIntent] method inside this block manually!
     *
     * For pre-context parameters version, to correctly call the child method, use `child.run { onUndeliveredIntent(it) }`
     *
     * See the [DecoratorBuilder] documentation for details on how this function behaves.
     */
    @FlowMVIDSL
    public fun onUndeliveredIntent(
        block: DecorateUndelivered<S, I, A, I>
    ): Unit = setOnce(::_onUndeliveredIntent, block)

    /**
     * Wraps the [StorePlugin.onUndeliveredAction] method of the child plugin passed in the [block] parameter.
     *
     * The child plugin will not know an event has occurred unless
     * you call the [StorePlugin.onUndeliveredAction] method inside this block manually!
     *
     * For pre-context parameters version, to correctly call the child method, use `child.run { onUndeliveredAction(e) }`
     *
     * See the [DecoratorBuilder] documentation for details on how this function behaves.
     */
    @FlowMVIDSL
    public fun onUndeliveredAction(
        block: DecorateUndelivered<S, I, A, A>
    ): Unit = setOnce(::_onUndeliveredAction, block)

    @PublishedApi
    internal fun build(): PluginDecorator<S, I, A> = PluginDecorator(
        name = name,
        onIntent = _onIntent,
        onState = _onState,
        onAction = _onAction,
        onException = _onException,
        onStart = _onStart,
        onSubscribe = _onSubscribe,
        onUnsubscribe = _onUnsubscribe,
        onStop = _onStop,
        onUndeliveredIntent = _onUndeliveredIntent,
        onUndeliveredAction = _onUndeliveredAction
    )
}
