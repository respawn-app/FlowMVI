package pro.respawn.flowmvi.sample.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.qualifier
import org.koin.dsl.bind
import org.koin.dsl.module
import pro.respawn.flowmvi.android.StoreViewModel
import pro.respawn.flowmvi.sample.features.xmlactivity.XmlActivityContainer
import pro.respawn.flowmvi.sample.navigation.AndroidFeatureLauncher
import pro.respawn.flowmvi.sample.platform.AndroidFileManager
import pro.respawn.flowmvi.sample.platform.FileManager
import pro.respawn.flowmvi.sample.platform.PlatformFeatureLauncher

actual val platformAppModule = module {
    singleOf(::AndroidFeatureLauncher) bind PlatformFeatureLauncher::class
    singleOf(::AndroidFileManager) bind FileManager::class
    factoryOf(::XmlActivityContainer)
    viewModel(qualifier<XmlActivityContainer>()) { StoreViewModel(get<XmlActivityContainer>()) }
}
