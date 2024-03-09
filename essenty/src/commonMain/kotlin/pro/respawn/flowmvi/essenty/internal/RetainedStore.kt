package pro.respawn.flowmvi.essenty.internal

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store

internal interface RetainedStore<S : MVIState, I : MVIIntent, A : MVIAction> : Store<S, I, A>, InstanceKeeper.Instance {

    override fun onDestroy(): Unit = close()
}
