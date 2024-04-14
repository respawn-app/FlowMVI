package pro.respawn.flowmvi.sample

import android.app.Application
import pro.respawn.flowmvi.sample.di.startKoin

class MVIApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin()
    }
}
