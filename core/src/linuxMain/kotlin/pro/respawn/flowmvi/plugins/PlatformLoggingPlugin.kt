package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin

public actual fun <S : MVIState, I : MVIIntent, A : MVIAction> platformLoggingPlugin(
    tag: String?,
    level: StoreLogLevel?
): StorePlugin<S, I, A> = consoleLoggingPlugin(tag)