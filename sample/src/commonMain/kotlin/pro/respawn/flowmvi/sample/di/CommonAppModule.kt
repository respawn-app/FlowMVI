package pro.respawn.flowmvi.sample.di

import org.koin.dsl.module
import pro.respawn.flowmvi.sample.arch.di.commonArchModule
import pro.respawn.flowmvi.sample.features.featureModule

val appModule = module {
    includes(
        platformAppModule,
        commonArchModule,
        featureModule,
    )
}
