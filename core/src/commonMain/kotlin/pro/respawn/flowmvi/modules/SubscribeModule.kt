package pro.respawn.flowmvi.modules

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.plugins.AbstractStorePlugin

internal interface SubscribeModule {

    val subscribers: StateFlow<Int>
}

internal class SubscriberModuleImpl(
    val onSubscribe: suspend (Int) -> Unit,
    val onUnsubscribe: suspend (Int) -> Unit,
) {

}
