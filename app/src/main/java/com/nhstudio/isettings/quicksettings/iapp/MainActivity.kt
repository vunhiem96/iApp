package com.nhstudio.isettings.quicksettings.iapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Choreographer
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.iaplibrary.IapConnector
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.nhstudio.iapp.appmanager.BuildConfig
import com.nhstudio.iapp.appmanager.databinding.ActivityMainBinding
import com.nhstudio.isettings.quicksettings.iapp.cmp.CMPCallback
import com.nhstudio.isettings.quicksettings.iapp.cmp.CMPController
import com.nhstudio.isettings.quicksettings.iapp.extension.LoadAppUtils
import com.nhstudio.isettings.quicksettings.iapp.extension.PRODUCT_ID
import com.nhstudio.isettings.quicksettings.iapp.extension.PRODUCT_ID_FAKE
import com.nhstudio.isettings.quicksettings.iapp.extension.beGone
import com.nhstudio.isettings.quicksettings.iapp.extension.beVisible
import com.nhstudio.isettings.quicksettings.iapp.extension.checkInter
import com.nhstudio.isettings.quicksettings.iapp.extension.config
import com.nhstudio.isettings.quicksettings.iapp.extension.darkMode
import com.nhstudio.isettings.quicksettings.iapp.extension.isTesting
import com.nhstudio.isettings.quicksettings.iapp.extension.loadInterAd
import com.nhstudio.isettings.quicksettings.iapp.extension.priceString
import com.nhstudio.isettings.quicksettings.iapp.extension.priceStringFake
import com.nhstudio.isettings.quicksettings.iapp.extension.showDialogAds
import com.nhstudio.isettings.quicksettings.iapp.extension.showInterOk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        runWhenFirstFrameDrawn{
            setUpAds()
            getIAP()
        }

    }

    fun runWhenFirstFrameDrawn(onDrawn: () -> Unit) {
        val rootView = window.decorView
        val handler = Handler(Looper.getMainLooper())
        var isCalled = false

        val drawListener = object : ViewTreeObserver.OnDrawListener {
            override fun onDraw() {
                if (isCalled) return
                isCalled = true

                handler.post {
                    Choreographer.getInstance().postFrameCallback {
                        rootView.viewTreeObserver.takeIf { it.isAlive }?.removeOnDrawListener(this)
                        onDrawn()
                    }
                }
            }
        }

        rootView.viewTreeObserver.addOnDrawListener(drawListener)
    }

    fun showDialogAd() {
        if (mInterstitialAdmob != null && config.pu && loadInterAd) {
            checkInter = true
            showDialogAds(this, lifecycle)
        }
    }
    fun showInter() {
        if (mInterstitialAdmob != null  && config.pu && loadInterAd) {
            checkInter = true
            mInterstitialAdmob?.show(this)
        }

    }

    private fun setUpAds() {
        if(config.pu) {
            CMPController(this).showCMP(BuildConfig.DEBUG,
                object : CMPCallback {
                    override fun onShowAd() {
                        if (config.pu) {
                            loadAds()
                            setupOpenAds()
                        }


                    }

                    override fun onChangeScreen() {
                        if (config.pu) {
                            loadAds()
                            setupOpenAds()
                        }


                    }
                })
        }
    }

     fun setupOpenAds() {
        if (config.pu) {
            Handler(Looper.getMainLooper()).postDelayed({
                AppOpenManager(
                    this,
                    lifecycle,
                    onShowOpenApp = {
                        binding.viewShowOpenApp.beVisible()
                    },
                    onCloseOpenApp = {
                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.viewShowOpenApp.beGone()
                        }, 0)

                    }, {

                    })
            },4000)

        }

    }

    private fun loadAds() {
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@MainActivity) {}
            runOnUiThread {
                loadInterAdmob()
            }
        }

    }

    private var mInterstitialAdmob: com.google.android.gms.ads.interstitial.InterstitialAd? = null

    var idAdmob = "ca-app-pub-9589105932398084/6656220956"

    private fun loadInterAdmob() {
        loadInterAd = false
        if (isTesting) {
            idAdmob = "ca-app-pub-3940256099942544/1033173712"

        }
        val adRequest = AdRequest.Builder().build()

        com.google.android.gms.ads.interstitial.InterstitialAd.load(
            this,
            idAdmob,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAdmob = null

                }

                override fun onAdLoaded(interstitialAd: com.google.android.gms.ads.interstitial.InterstitialAd) {
                    loadInterAd = true
                    mInterstitialAdmob = interstitialAd
                    mInterstitialAdmob?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdClicked() {
                               checkInter = true
                            }

                            override fun onAdDismissedFullScreenContent() {
                                loadInterAd = false
                                showInterOk = true
                                mInterstitialAdmob = null
//                                isShowInter = false
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: com.google.android.gms.ads.AdError) {
                                try {
//                                    adsDialog.dialog.dismiss()

                                } catch (e: Exception) {

                                }
                            }

                            override fun onAdImpression() {
                                loadInterAd = false
                                showInterOk = true
                            }

                            override fun onAdShowedFullScreenContent() {
                                try {
                                    checkInter = true
                                    loadInterAd = false
//                                    isShowInter = true
//                                    adsDialog.dialog.dismiss()

                                } catch (e: Exception) {

                                }
                            }
                        }
                }
            })

    }

    override fun onResume() {
        super.onResume()
        setUpColor()
    }

    private fun setUpColor() {
        if (config.darkMode == 2) {
            val rightNow: Calendar = Calendar.getInstance()
            val currentHourIn24Format: Int = rightNow.get(Calendar.HOUR_OF_DAY)
            if (currentHourIn24Format in 6..17) {
                darkMode = false
            } else {
                darkMode = true
            }

        } else if (config.darkMode == 0) {
            darkMode = true
        }
        binding.isLight = !darkMode
    }
    fun resetIAP() {
        if (BuildConfig.DEBUG) {
            IapConnector.resetIap(this)
        }
    }

    private fun getIAP() {
        IapConnector.listPurchased.observe(this) {
            val data = IapConnector.getAllProductModel()

            data.forEach { product ->
                if (product.productId == PRODUCT_ID) {
                    priceString = product.inAppDetails?.formattedPrice.toString()
                }
                if (product.productId == PRODUCT_ID_FAKE) {
                    priceStringFake = product.inAppDetails?.formattedPrice.toString()
                }
            }
            it?.let {
                CoroutineScope(Dispatchers.Main).launch {
                    config.pu = it.isEmpty()
                }
            }
        }
    }

    fun buyIAP() {
        try {
            IapConnector.buyIap(this, PRODUCT_ID)
        } catch (e: Exception) {
            Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show()
        }

    }
}