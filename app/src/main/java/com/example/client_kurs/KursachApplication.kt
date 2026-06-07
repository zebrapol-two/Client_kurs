package com.example.client_kurs

import android.app.Application
import com.example.client_kurs.di.appModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class KursachApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        startKoin {
            androidContext(this@KursachApplication)
            modules(appModule)
        }
    }
}