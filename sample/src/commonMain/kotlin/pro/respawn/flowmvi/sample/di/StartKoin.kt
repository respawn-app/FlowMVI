package pro.respawn.flowmvi.sample.di

import org.koin.core.KoinApplication
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import pro.respawn.flowmvi.logging.PlatformStoreLogger
import pro.respawn.flowmvi.logging.StoreLogLevel

private val Level.asStoreLogLevel
    get() = when (this) {
        Level.NONE -> StoreLogLevel.Trace
        Level.DEBUG -> StoreLogLevel.Debug
        Level.INFO -> StoreLogLevel.Info
        Level.WARNING -> StoreLogLevel.Warn
        Level.ERROR -> StoreLogLevel.Error
    }

@PublishedApi
internal val KoinLogger = object : Logger() {
    override fun display(level: Level, msg: String) {
        PlatformStoreLogger.log(level.asStoreLogLevel, "Koin") { msg }
    }
}

inline fun startKoin(
    modules: List<Module> = emptyList(),
    crossinline configure: KoinAppDeclaration = { }
): KoinApplication = org.koin.core.context.startKoin {
    logger(KoinLogger)
    modules(modules + appModule)
    // coroutinesEngine()
    configure()
    createEagerInstances()
}
