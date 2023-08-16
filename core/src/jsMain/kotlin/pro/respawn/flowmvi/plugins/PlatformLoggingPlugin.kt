package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin

public actual fun <S : MVIState, I : MVIIntent, A : MVIAction> platformLoggingPlugin(
    tag: String?,
    level: StoreLogLevel?
): StorePlugin<S, I, A> = consoleLoggingPlugin(tag, level)

public fun <S : MVIState, I : MVIIntent, A : MVIAction> consoleLoggingPlugin(
    tag: String?,
    level: StoreLogLevel?
): StorePlugin<S, I, A> = loggingPlugin(tag) { defaultLevel, t, msg ->
    val logLevel = level ?: defaultLevel
    val template = "$t : $msg"
    with(console) {
        when (logLevel) {
            StoreLogLevel.Trace, StoreLogLevel.Debug -> log(template)
            StoreLogLevel.Info -> info(template)
            StoreLogLevel.Warn -> warn(template)
            StoreLogLevel.Error -> error(template)
        }
    }
}
