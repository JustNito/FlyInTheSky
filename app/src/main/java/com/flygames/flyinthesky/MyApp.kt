package com.flygames.flyinthesky

import android.app.Application
import com.travelgames.roadrace.data.Storage

class MyApp : Application() {
    lateinit var storage: Storage
    override fun onCreate() {
        super.onCreate()
        storage = Storage(applicationContext)
    }
}