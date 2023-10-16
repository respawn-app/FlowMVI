package pro.respawn.flowmvi.plugins

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.getAndUpdate
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin

private const val DisallowRestartMessage = """
Store was disallowed to restart but was restarted. Please remove disallowRestartPlugin() or do not reuse the store.
"""

private const val DisallowRestartPluginName = "DisallowRestartPlugin"

/**
 * Disallow restart plugin will allow the store to be [pro.respawn.flowmvi.api.Store.start]ed only once.
 * It will throw on any subsequent invocations of [StorePlugin.onStart].
 * You can use this when you are sure that you are not going to restart your store.
 * I.e. you are using the scope with which you launch the store only once, such as viewModelScope on Android.
 *
 * There is no need to install this plugin multiple times so the plugin has a unique [StorePlugin.name].
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> disallowRestartPlugin(): StorePlugin<S, I, A> = plugin {
    name = DisallowRestartPluginName
    val started = atomic(false)
    onStart {
        check(!started.getAndUpdate { true }) { DisallowRestartMessage }
    }
}

/**
 * Installs a new [disallowRestartPlugin]. Please consult the docs of the parent function to learn more.
 * This plugin can only be installed only once.
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.disallowRestart(): Unit =
    install(disallowRestartPlugin())
