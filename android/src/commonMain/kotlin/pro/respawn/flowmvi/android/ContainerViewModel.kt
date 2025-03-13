package pro.respawn.flowmvi.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store

/**
 * A class that wraps a [container] in a [ViewModel], and (if [start] is true) starts the container's [store].
 *
 * The store is started at creation using a [viewModelScope].
 */
public open class ContainerViewModel<T : Container<S, I, A>, S : MVIState, I : MVIIntent, A : MVIAction>(
    public val container: T,
    start: Boolean = true,
) : ViewModel(), Store<S, I, A> by container.store, Container<S, I, A> by container {

    init {
        if (start) addCloseable(store.start(viewModelScope))
    }
}
