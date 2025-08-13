package pro.respawn.flowmvi.debugger.server.di

import org.koin.core.context.GlobalContext

val koin by lazy {
    GlobalContext.startKoin {
        modules(appModule, platformModule)
    }.koin
}
