package com.tpgszhq.jh

import android.app.Application
import com.tpgszhq.jh.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ImageConverterApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@ImageConverterApplication)
            modules(appModule)
        }
    }
}
