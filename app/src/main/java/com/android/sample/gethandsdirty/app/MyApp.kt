package com.android.sample.gethandsdirty.app

import android.app.Application

class MyApp : Application() {

    companion object {
        private lateinit var myApp: MyApp
        fun myApp(): Application {
            return myApp;
        }
    }

    override fun onCreate() {
        super.onCreate()
        myApp = this
    }
}