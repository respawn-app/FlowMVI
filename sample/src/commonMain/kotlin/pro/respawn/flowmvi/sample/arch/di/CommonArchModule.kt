package pro.respawn.flowmvi.sample.arch.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import pro.respawn.flowmvi.sample.arch.configuration.DefaultConfigurationFactory
import pro.respawn.flowmvi.sample.arch.configuration.ConfigurationFactory

val commonArchModule: Module = module {
    singleOf(::DefaultConfigurationFactory) bind ConfigurationFactory::class
}
