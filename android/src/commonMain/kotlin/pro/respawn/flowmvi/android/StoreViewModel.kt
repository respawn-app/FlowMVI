package pro.respawn.flowmvi.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store

/**
 * An extendable class for creating a [ViewModel] that directly uses a delegate to act as a [Store].
 * There are 2 possible ways to use this class:
 *
 * 1. Create StoreViewModels and inject your desired [Store] as a constructor parameter,
 * then use a qualified name or other type of dependency injection to resolve the needed ViewModel.
 * 2. Subclass [StoreViewModel] for each ViewModel that is being used, build the [Store] using constructor params,
 * with assistance from DI, and then inject that ViewModel by its direct type.
 *
 * Most DI frameworks struggle with type erasure when it comes to injecting generic classes, so one of 2 ways outlined
 * above can be used to resolve the conflicts.
 */
public open class StoreViewModel<S : MVIState, I : MVIIntent, A : MVIAction>(
    store: Store<S, I, A>,
    start: Boolean = true,
) : ViewModel(), Store<S, I, A> by store, Container<S, I, A> {

    @Deprecated("If you want to use Containers, prefer a more flexible \"ContainerViewModel\" instead")
    public constructor(container: Container<S, I, A>, start: Boolean = true) : this(container.store, start)

    final override val store: Store<S, I, A> get() = this

    init {
        if (start) store.start(viewModelScope)
    }
}
