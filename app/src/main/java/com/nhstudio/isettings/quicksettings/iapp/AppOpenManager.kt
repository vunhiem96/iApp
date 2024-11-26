package com.nhstudio.isettings.quicksettings.iapp

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.nhstudio.isettings.quicksettings.iapp.extension.canShowOpenAds
import com.nhstudio.isettings.quicksettings.iapp.extension.checkInter
import com.nhstudio.isettings.quicksettings.iapp.extension.config
import com.nhstudio.isettings.quicksettings.iapp.extension.isTesting


class AppOpenManager(
    val activity: Activity,
    lifecycle: Lifecycle,
    val onShowOpenApp: () -> Unit,
    val onCloseOpenApp: () -> Unit,
    val onRecent: () -> Unit
) {
    private val AD_UNIT_ID = "ca-app-pub-9589105932398084/4030057610"
    private val AD_UNIT_ID_DEBUG = "ca-app-pub-3940256099942544/9257395921"
    var appOpenAd: AppOpenAd? = null
    private var loadCallback: AppOpenAd.AppOpenAdLoadCallback? = null
    var showOpenAds = false
    var isLoading = false
    var loadOpenAdsCount = 0

    init {
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    Log.e("TAG", "AppOpenManager: ON_START" )
                    showAdIfAvailable()
                }

                Lifecycle.Event.ON_DESTROY -> {
                    appOpenAd = null
                }

                else -> {

                }
            }
        })
    }

    private fun showAdIfAvailable() {
        if (isAdAvailable() && !checkInter && canShowOpenAds ) {
            onRecent()
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        appOpenAd = null
                        onCloseOpenApp()
                        checkInter = true
                        Handler(Looper.getMainLooper()).postDelayed({
                            showOpenAds = false
                        },12000)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {

                    }

                    override fun onAdShowedFullScreenContent() {
                        checkInter = true
                        onShowOpenApp()

                    }

                    override fun onAdClicked() {

                    }
                }
            appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
            appOpenAd?.show(activity)
        } else {
            fetchAd()
        }
    }
    fun fetchAd() {
        if (showOpenAds||isAdAvailable() || isLoading || !activity.config.pu) {
            return
        }

        loadCallback =
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(p0: AppOpenAd) {
                    super.onAdLoaded(p0)
                    showOpenAds = true
                    appOpenAd = p0
                    isLoading = false
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    isLoading = false
                }
            }

        val request = getAdRequest()
        if(loadOpenAdsCount<3){
            isLoading = true
            if (isTesting) {
                AppOpenAd.load(
                    activity,
                    AD_UNIT_ID_DEBUG,
                    request,
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                    loadCallback as AppOpenAd.AppOpenAdLoadCallback
                )
            } else {
                AppOpenAd.load(
                    activity,
                    AD_UNIT_ID,
                    request,
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                    loadCallback as AppOpenAd.AppOpenAdLoadCallback
                )
            }
        }
        loadOpenAdsCount+=1

    }

    private fun getAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null
    }
}
