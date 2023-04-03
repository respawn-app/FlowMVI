package pro.respawn.flowmvi.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIProvider
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.MVIStore

public open class StoreViewModel<S : MVIState, I : MVIIntent, A : MVIAction>(
    store: MVIStore<S, I, A>,
) : ViewModel(), MVIProvider<S, I, A> by store {

    init {
        store.start(viewModelScope)
    }
}
