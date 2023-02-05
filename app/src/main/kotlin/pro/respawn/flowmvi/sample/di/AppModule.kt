package pro.respawn.flowmvi.sample.di

import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import pro.respawn.flowmvi.sample.compose.BaseClassViewModel
import pro.respawn.flowmvi.sample.repo.CounterRepo
import pro.respawn.flowmvi.sample.view.NoBaseClassViewModel

val appModule = module {
    singleOf(::CounterRepo)
    viewModelOf(::BaseClassViewModel)
    viewModelOf(::NoBaseClassViewModel)
}
