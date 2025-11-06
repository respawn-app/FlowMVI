@file:MustUseReturnValue

package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.dsl.updateStateImmediate

private const val Name = "ResetStatePlugin"

/**
 * Normally, [Store]s do not reset their state back to the initial value when they are stopped.
 *
 * This plugin sets the state back to the initial value (defined in the builder) each time the store is stopped.
 *
 * Install this plugin first preferably.
 *
 * **This plugin can only be installed ONCE**.
 **/
public fun <S : MVIState, I : MVIIntent, A : MVIAction> resetStatePlugin(): StorePlugin<S, I, A> = plugin {
    this.name = Name
    onStop { e ->
        updateStateImmediate { config.initial }
    }
}

/**
 * Install a new [resetStatePlugin].
 **/
@IgnorableReturnValue
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.resetStateOnStop(): Unit = install(
    resetStatePlugin()
)
