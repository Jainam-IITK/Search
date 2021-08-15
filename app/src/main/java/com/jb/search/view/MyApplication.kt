package com.jb.search.view

import android.app.Application
import kotlinx.coroutines.IO_PARALLELISM_PROPERTY_NAME

class MyApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        System.setProperty(IO_PARALLELISM_PROPERTY_NAME, Integer.toString(1000));

    }
}