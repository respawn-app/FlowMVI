package pro.respawn.flowmvi.sample.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module
import pro.respawn.flowmvi.sample.arch.di.commonArchModule
import pro.respawn.flowmvi.sample.features.featureModule
import pro.respawn.flowmvi.sample.util.Json

val appModule = module {
    single { Json }
    single { ApplicationScope(CoroutineScope(SupervisorJob())) }
    includes(
        platformAppModule,
        commonArchModule,
        featureModule,
    )
}
