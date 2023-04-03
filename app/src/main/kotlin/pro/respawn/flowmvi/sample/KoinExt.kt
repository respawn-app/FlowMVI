package pro.respawn.flowmvi.sample

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.definition.Definition
import org.koin.core.module.Module
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.MVIStore
import pro.respawn.flowmvi.android.StoreViewModel

open class ProviderClass<S : MVIState, I : MVIIntent, A : MVIAction> {

    private val name = requireNotNull(this::class.qualifiedName) { "Can't use anonymous class as a Provider" }

    val qualifier = named(name)
}

inline fun <S : MVIState, I : MVIIntent, A : MVIAction, reified P : MVIStore<S, I, A>> Module.storeViewModel(
    clazz: ProviderClass<S, I, A>,
    noinline parameters: ParametersDefinition? = null,
) = viewModel(clazz.qualifier) { StoreViewModel(get<P>(clazz.qualifier, parameters)) } bind StoreViewModel::class

inline fun <S : MVIState, I : MVIIntent, A : MVIAction, reified P : MVIStore<S, I, A>> Module.provider(
    clazz: ProviderClass<S, I, A>,
    noinline definition: Definition<P>
) = factory(qualifier = clazz.qualifier, definition = definition) bind MVIStore::class
