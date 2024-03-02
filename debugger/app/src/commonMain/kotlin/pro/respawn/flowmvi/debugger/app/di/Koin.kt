package pro.respawn.flowmvi.debugger.app.di

import org.koin.core.context.GlobalContext

val koin by lazy {
    GlobalContext.startKoin {
        modules(appModule)
        createEagerInstances()
    }.koin
}
