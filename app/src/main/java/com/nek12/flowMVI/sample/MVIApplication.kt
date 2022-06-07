package com.nek12.flowMVI.sample

import android.app.Application
import com.nek12.flowMVI.sample.di.appModule
import org.koin.core.context.startKoin

class MVIApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(appModule)
        }
    }
}
