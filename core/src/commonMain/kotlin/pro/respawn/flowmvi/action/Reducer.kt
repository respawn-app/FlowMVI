@file:Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION")

package pro.respawn.flowmvi.action

import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState

public fun interface Reducer<S : MVIState, I : MVIIntent, A : MVIAction> {

    public suspend operator fun SuspendPipelineContext<S, I, A>.invoke(intent: I)
}
