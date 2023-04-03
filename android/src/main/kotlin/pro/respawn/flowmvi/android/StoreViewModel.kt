package pro.respawn.flowmvi.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIProvider
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.MVIStore

/**
 * An extendable class for creating a [ViewModel] that directly uses an [MVIStore] to act as an [MVIProvider].
 * There are 2 possible ways to use this class:
 *
 * 1. Create StoreViewModels and inject your desired [MVIStore] as a constructor parameter,
 * then use a qualified name or other type of dependency injection to resolve the needed ViewModel.
 * 2. Subclass [StoreViewModel] for each ViewModel that is being used, build the [MVIStore] using constructor params,
 * with assistance from DI, and then inject that ViewModel by its direct type.
 *
 * Most DI frameworks struggle with type erasure when it comes to injecting generic classes, so one of 2 ways outlined
 * above can be used to resolve the conflicts.
 */
public open class StoreViewModel<S : MVIState, I : MVIIntent, A : MVIAction>(
    store: MVIStore<S, I, A>,
) : ViewModel(), MVIProvider<S, I, A> by store {

    init {
        store.start(viewModelScope)
    }
}
