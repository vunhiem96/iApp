package com.nhstudio.isettings.quicksettings.iapp.cmp

import android.app.Activity
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class CMPController  constructor(private var activity: Activity) {

//    private lateinit var activity: Activity
//
//    init {
//    }

    init {


    }


    fun isGDPR(): Boolean {
        return GDPRUtils.isGDPR(activity)
    }


    fun showCMP() {
        UserMessagingPlatform.loadConsentForm(activity, {
//            dialog.dismissDialog()
            it.show(activity) {
                val canRequestAds = GDPRUtils.canShowAds(activity)
//                if (canRequestAds) {
//                    AdsUtils.isNotConsent = false
//                } else {
//                    AdsUtils.isNotConsent = true
//                }
            }
        }, {
//            dialog.dismissDialog()
        })
    }
    fun showCMP(isTesting: Boolean) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        if (consentInformation.isConsentFormAvailable) {
            UserMessagingPlatform.loadConsentForm(activity,
                {
                    it.show(activity) {
                    }
                }, {
                })

        } else {
            showCMP(isTesting, cmpCallback = object : CMPCallback {
                override fun onShowAd() {
                }

                override fun onChangeScreen() {

                }
            })
        }

    }

    fun showCMP(isTesting: Boolean, cmpCallback: CMPCallback) {
        val codeGDPR = GDPRUtils.isGDPR2(activity)
        if (codeGDPR == 0) {
            cmpCallback.onShowAd()
        } else {
            val debugSettings = ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId(Utils.getDeviceID(activity)).build()

            val params = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false)
                .setConsentDebugSettings(if (isTesting) debugSettings else null).build()

            val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
            consentInformation.requestConsentInfoUpdate(activity, params, {
                val canRequestAds = GDPRUtils.canShowAds(activity)
                if (canRequestAds) {
                    GDPRUtils.isUserConsent = true
                    cmpCallback.onShowAd()
                } else {
                    UserMessagingPlatform.loadConsentForm(activity, {
                        it.show(activity) {
                            val canRequestAds = GDPRUtils.canShowAds(activity)
                            if (canRequestAds) {
                                GDPRUtils.isUserConsent = true
                                cmpCallback.onShowAd()
//                            showAd()
                            } else {
                                GDPRUtils.isUserConsent = false
                                cmpCallback.onChangeScreen()

//                            changeScreen()
                            }
                        }
                    }, {
                        cmpCallback.onShowAd()
                    })
                }
            }, { requestConsentError ->
                cmpCallback.onChangeScreen()
            })
        }

    }
    fun showCMP2(isTesting: Boolean, cmpCallback: CMPCallback) {
            val debugSettings = ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId(Utils.getDeviceID(activity)).build()

            val params = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false)
                .setConsentDebugSettings(if (isTesting) debugSettings else null).build()

            val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
            consentInformation.requestConsentInfoUpdate(activity, params, {
                val canRequestAds = GDPRUtils.canShowAds(activity)
                    UserMessagingPlatform.loadConsentForm(activity, {
                        it.show(activity) {
                            val canRequestAds = GDPRUtils.canShowAds(activity)
                            if (canRequestAds) {
                                GDPRUtils.isUserConsent = true
                                cmpCallback.onShowAd()
//                            showAd()
                            } else {
                                GDPRUtils.isUserConsent = false
                                cmpCallback.onChangeScreen()

//                            changeScreen()
                            }
                        }
                    }, {
                        cmpCallback.onShowAd()
                    })

            }, { requestConsentError ->
                cmpCallback.onChangeScreen()
            })


    }


    companion object {

//        @SuppressLint("StaticFieldLeak")
//        private lateinit var cmpController: CMPController
//
//
//        fun init(
//            activity: Activity,
//        ) {
//            cmpController = CMPController(activity)
//        }
//
//
//        fun getInstance(): CMPController {
//            if (!::cmpController.isInitialized) {
//                throw Throwable("call init")
//            }
//            return cmpController
//        }
//
//        fun checkInit() = ::cmpController.isInitialized
    }
}