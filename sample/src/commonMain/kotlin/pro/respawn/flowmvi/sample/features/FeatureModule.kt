package pro.respawn.flowmvi.sample.features

import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import pro.respawn.flowmvi.sample.arch.di.container
import pro.respawn.flowmvi.sample.features.home.HomeContainer
import pro.respawn.flowmvi.sample.features.lce.LCEContainer
import pro.respawn.flowmvi.sample.features.lce.LCERepository

val featureModule = module {
    singleOf(::LCERepository)
    container { new(::HomeContainer) }
    container { new(::LCEContainer) }
}
