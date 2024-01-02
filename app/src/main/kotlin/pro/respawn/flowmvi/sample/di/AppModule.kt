package pro.respawn.flowmvi.sample.di

import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import pro.respawn.flowmvi.sample.compose.CounterContainer
import pro.respawn.flowmvi.sample.repository.CounterRepository
import pro.respawn.flowmvi.sample.view.LambdaViewModel

val appModule = module {
    singleOf(::CounterRepository)
    viewModelOf(::LambdaViewModel)

    factoryOf(::CounterContainer)
    storeViewModel<CounterContainer>()
    single { Json { ignoreUnknownKeys = true } }
}
