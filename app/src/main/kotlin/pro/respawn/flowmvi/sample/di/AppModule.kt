package pro.respawn.flowmvi.sample.di

import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import pro.respawn.flowmvi.sample.repo.CounterRepo
import pro.respawn.flowmvi.sample.provider.CounterProvider

val appModule = module {
    singleOf(::CounterRepo)
    // or instead simply create a subclass of StoreViewModel
    provider(CounterProvider) { new(::CounterProvider).store }
    storeViewModel(CounterProvider)
}
