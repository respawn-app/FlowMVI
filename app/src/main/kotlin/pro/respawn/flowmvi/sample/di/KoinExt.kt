package pro.respawn.flowmvi.sample.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.definition.Definition
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import pro.respawn.flowmvi.android.StoreViewModel
import pro.respawn.flowmvi.api.Store

open class ProviderClass {

    val name = requireNotNull(this::class.qualifiedName) { "Can't use anonymous class as a Provider" }
    val qualifier = named(name)
}

fun Module.storeViewModel(
    klass: ProviderClass,
) = viewModel(klass.qualifier) {
    StoreViewModel(get<Store<*, *, *>>(klass.qualifier) { it })
} bind StoreViewModel::class

fun Module.provider(
    klass: ProviderClass,
    definition: Definition<Store<*, *, *>>,
) = factory(klass.qualifier, definition) bind Store::class
