package pro.respawn.flowmvi.decompose.api

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

internal interface RetainedScope : CoroutineScope, InstanceKeeper.Instance {

    override fun onDestroy(): Unit = cancel()
}
