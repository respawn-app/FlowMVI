package pro.respawn.flowmvi.sample.features

import org.koin.core.module.dsl.new
import org.koin.dsl.module
import pro.respawn.flowmvi.sample.arch.di.container
import pro.respawn.flowmvi.sample.features.home.HomeContainer

val featureModule = module {
    container { new(::HomeContainer) }
}
