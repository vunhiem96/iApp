package com.nhstudio.isettings.quicksettings.iapp

import android.app.Application
import com.example.iaplibrary.IapConnector
import com.nhstudio.iapp.appmanager.BuildConfig
import com.nhstudio.isettings.quicksettings.iapp.extension.LoadAppUtils

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        LoadAppUtils.init(this)
        IapConnector.initIap(this, "iap_id.json",10000L, BuildConfig.DEBUG)
    }
}
