package pro.respawn.flowmvi.sample.di

import android.app.Application
import org.koin.android.ext.koin.androidContext

fun Application.startKoin() = startKoin builder@{
    androidContext(this@startKoin)
    // workManagerFactory()
}
