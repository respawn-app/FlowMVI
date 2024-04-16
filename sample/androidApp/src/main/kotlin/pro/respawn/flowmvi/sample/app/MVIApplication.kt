package pro.respawn.flowmvi.sample.app

import android.app.Application
import pro.respawn.flowmvi.sample.di.startKoin

class MVIApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin()
    }
}
