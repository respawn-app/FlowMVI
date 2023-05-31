package pro.respawn.flowmvi.sample.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.definition.Definition
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.store.MVIStore
import pro.respawn.flowmvi.android.StoreViewModel

open class ProviderClass<S : MVIState, I : MVIIntent, A : MVIAction> {

    private val name = requireNotNull(this::class.qualifiedName) { "Can't use anonymous class as a Provider" }

    val qualifier = named(name)
}

inline fun <S : MVIState, I : MVIIntent, A : MVIAction, reified P : MVIStore<S, I, A>> Module.storeViewModel(
    klass: ProviderClass<S, I, A>,
) = viewModel(klass.qualifier) { StoreViewModel(get<P>(klass.qualifier) { it }) } bind StoreViewModel::class

inline fun <S : MVIState, I : MVIIntent, A : MVIAction, reified P : MVIStore<S, I, A>> Module.provider(
    klass: ProviderClass<S, I, A>,
    noinline definition: Definition<P>
) = factory(klass.qualifier, definition) bind MVIStore::class
