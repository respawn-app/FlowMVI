package pro.respawn.flowmvi.sample.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import pro.respawn.flowmvi.sample.platform.BrowserFileManager
import pro.respawn.flowmvi.sample.platform.FileManager
import pro.respawn.flowmvi.sample.platform.NoOpPlatformFeatureLauncher
import pro.respawn.flowmvi.sample.platform.PlatformFeatureLauncher

actual val platformAppModule: Module = module {
    single { NoOpPlatformFeatureLauncher } bind PlatformFeatureLauncher::class
    singleOf(::BrowserFileManager) bind FileManager::class
}
