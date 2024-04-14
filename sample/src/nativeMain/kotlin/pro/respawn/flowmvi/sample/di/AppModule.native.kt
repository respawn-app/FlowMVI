package pro.respawn.flowmvi.sample.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import pro.respawn.flowmvi.sample.platform.NativeFileManager
import pro.respawn.flowmvi.sample.platform.FileManager

actual val platformAppModule = module {
    singleOf(::NativeFileManager) bind FileManager::class
}
