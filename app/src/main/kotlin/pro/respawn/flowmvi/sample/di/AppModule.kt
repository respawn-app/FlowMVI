package pro.respawn.flowmvi.sample.di

import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import pro.respawn.flowmvi.sample.provider.CounterContainer
import pro.respawn.flowmvi.sample.provider.CounterViewModel
import pro.respawn.flowmvi.sample.provider.LambdaViewModel
import pro.respawn.flowmvi.sample.repo.CounterRepo

val appModule = module {
    singleOf(::CounterRepo)
    factoryOf(::CounterContainer)
    viewModelOf(::CounterViewModel)
    viewModelOf(::LambdaViewModel)
}
