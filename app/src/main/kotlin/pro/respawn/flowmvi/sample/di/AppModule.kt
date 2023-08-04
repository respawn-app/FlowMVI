package pro.respawn.flowmvi.sample.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module
import pro.respawn.flowmvi.android.StoreViewModel
import pro.respawn.flowmvi.sample.container.CounterContainer
import pro.respawn.flowmvi.sample.container.CounterViewModel
import pro.respawn.flowmvi.sample.container.LambdaViewModel
import pro.respawn.flowmvi.sample.repo.CounterRepo

val appModule = module {
    singleOf(::CounterRepo)
    factoryOf(::CounterContainer)
    viewModelOf(::CounterViewModel)
    viewModelOf(::LambdaViewModel)
    viewModel(qualifier<CounterContainer>()) { StoreViewModel(store = get<CounterContainer>().store) }
}
