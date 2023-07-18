package pro.respawn.flowmvi.sample.di

import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import pro.respawn.flowmvi.sample.compose.BaseClassViewModel
import pro.respawn.flowmvi.sample.repo.CounterRepo
import pro.respawn.flowmvi.sample.view.BasicProvider

val appModule = module {
    singleOf(::CounterRepo)
    viewModelOf(::BaseClassViewModel)
    // or instead simply create a subclass of StoreViewModel
    provider(BasicProvider) { new(::BasicProvider).store }
    storeViewModel(BasicProvider)
}
