package pro.respawn.flowmvi.decompose.api

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store

public interface RetainedStore<S : MVIState, I : MVIIntent, A : MVIAction> : Store<S, I, A>, InstanceKeeper.Instance {

    override fun onDestroy(): Unit = close()
}
