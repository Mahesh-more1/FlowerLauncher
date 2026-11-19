package com.flowerlauncher

import android.app.Application
import android.content.Context

class FlowerApp : Application() {

    companion object {
        lateinit var instance: FlowerApp
            private set

        fun getContext(): Context = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
