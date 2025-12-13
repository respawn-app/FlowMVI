package pro.respawn.flowmvi.debugger.server.di

import io.ktor.serialization.kotlinx.json.DefaultJson
import org.koin.core.module.dsl.new
import org.koin.dsl.module
import pro.respawn.flowmvi.debugger.server.arch.configuration.DefaultStoreConfiguration
import pro.respawn.flowmvi.debugger.server.arch.configuration.StoreConfiguration
import pro.respawn.flowmvi.debugger.server.ui.screens.connect.ConnectContainer
import pro.respawn.flowmvi.debugger.server.ui.screens.storedetails.StoreDetailsContainer
import pro.respawn.flowmvi.debugger.server.ui.screens.storemetrics.StoreMetricsContainer
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineContainer

val appModule = module {
    single { DefaultJson }
    single<StoreConfiguration> { DefaultStoreConfiguration(get(), get()) }
    container { new(::ConnectContainer) }
    container { new(::TimelineContainer) }
    container { new(::StoreDetailsContainer) }
    container { new(::StoreMetricsContainer) }
}
