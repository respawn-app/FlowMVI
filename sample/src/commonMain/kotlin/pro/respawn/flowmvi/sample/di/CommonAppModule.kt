package pro.respawn.flowmvi.sample.di

import org.koin.dsl.module
import org.koin.dsl.onClose
import pro.respawn.flowmvi.sample.arch.di.commonArchModule
import pro.respawn.flowmvi.sample.features.featureModule
import pro.respawn.flowmvi.sample.util.Json

val appModule = module {
    single { Json }
    single { ApplicationScope(parent = null) } onClose { it?.close() }
    includes(
        platformAppModule,
        commonArchModule,
        featureModule,
    )
}
