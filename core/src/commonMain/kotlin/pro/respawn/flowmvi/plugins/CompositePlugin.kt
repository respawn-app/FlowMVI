@file:pro.respawn.flowmvi.annotation.MustUseReturnValues

package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.impl.plugin.asInstance
import pro.respawn.flowmvi.impl.plugin.compose

/**
 * A plugin that delegates to [plugins] in the iteration order.
 *
 * This can be used to make several plugins appear as a single one for consumers, for example, when a plugin
 * requires another plugin to be installed.
 *
 * This is an implementation of the "Composite" pattern and the "Chain or Responsibility" pattern.
 *
 * The [plugins] list must support random element access in order to be performant
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> compositePlugin(
    plugins: List<StorePlugin<S, I, A>>,
    name: String? = null,
): StorePlugin<S, I, A> = plugins.map(StorePlugin<S, I, A>::asInstance).compose(name)

/**
 * A plugin that delegates to [first] and [other] in the declaration order.
 *
 * This can be used to make several plugins appear as a single one for consumers, for example, when a plugin
 * requires another plugin to be installed.
 *
 * This is an implementation of the "Composite" pattern and the "Chain or Responsibility" pattern.
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> compositePlugin(
    first: StorePlugin<S, I, A>,
    vararg other: StorePlugin<S, I, A>,
    name: String? = null,
): StorePlugin<S, I, A> = sequenceOf(first).plus(other).map(StorePlugin<S, I, A>::asInstance).toList().compose(name)
