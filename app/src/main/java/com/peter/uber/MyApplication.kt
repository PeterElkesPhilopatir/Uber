package com.peter.uber

import android.app.Application
import com.peter.awesomenews.di.fireStoreModule
import com.peter.awesomenews.di.locationModule
import com.peter.awesomenews.di.mainViewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(listOf(mainViewModelModule,fireStoreModule,locationModule))
        }
    }
}