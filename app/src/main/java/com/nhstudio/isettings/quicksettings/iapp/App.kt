package com.nhstudio.isettings.quicksettings.iapp

import android.app.Application
import com.example.iaplibrary.IapConnectorV2
import com.nhstudio.iapp.appmanager.BuildConfig
import com.nhstudio.isettings.quicksettings.iapp.extension.LoadAppUtils

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        LoadAppUtils.init(this)
        IapConnectorV2.initIap(this, "iap_id.json",15000L, BuildConfig.DEBUG)
    }
}
