package pro.respawn.flowmvi.sample.arch.configuration

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.debugger.plugin.DebuggerSink
import pro.respawn.flowmvi.debugger.plugin.enableRemoteDebugging
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.logging.PlatformStoreLogger
import pro.respawn.flowmvi.logging.invoke

actual fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.remoteDebugger() = enableRemoteDebugging()
actual fun metricsSink() = DebuggerSink { PlatformStoreLogger(it) }
