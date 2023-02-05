package pro.respawn.flowmvi.sample

import android.app.Application
import org.koin.core.context.startKoin
import pro.respawn.flowmvi.sample.di.appModule

class MVIApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(appModule)
        }
    }
}
