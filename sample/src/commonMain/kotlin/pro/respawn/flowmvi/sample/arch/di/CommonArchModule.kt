package pro.respawn.flowmvi.sample.arch.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import pro.respawn.flowmvi.sample.arch.configuration.DefaultStoreConfiguration
import pro.respawn.flowmvi.sample.arch.configuration.StoreConfiguration

val commonArchModule: Module = module {
    singleOf(::DefaultStoreConfiguration) bind StoreConfiguration::class
}
