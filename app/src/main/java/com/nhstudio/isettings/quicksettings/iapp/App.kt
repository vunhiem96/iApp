package com.nhstudio.isettings.quicksettings.iapp

import android.app.Application
import com.example.iaplibrary.IapConnectorV2
import com.google.android.gms.ads.MobileAds
import com.nhstudio.iapp.appmanager.BuildConfig
import com.nhstudio.isettings.quicksettings.iapp.extension.LoadAppUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@App) {
            }
        }
        LoadAppUtils.init(this)
        IapConnectorV2.initIap(this, "iap_id.json",15000L, BuildConfig.DEBUG)
    }
}
