package com.nhstudio.isettings.quicksettings.iapp.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.nhstudio.iapp.appmanager.R
import com.nhstudio.iapp.appmanager.databinding.FragmentUserBinding
import com.nhstudio.isettings.quicksettings.iapp.MainActivity
import com.nhstudio.isettings.quicksettings.iapp.extension.beGone
import com.nhstudio.isettings.quicksettings.iapp.extension.canShowOpenAds
import com.nhstudio.isettings.quicksettings.iapp.extension.checkInter
import com.nhstudio.isettings.quicksettings.iapp.extension.config
import com.nhstudio.isettings.quicksettings.iapp.extension.darkMode
import com.nhstudio.isettings.quicksettings.iapp.extension.haveInternet
import com.nhstudio.isettings.quicksettings.iapp.extension.isTesting
import com.nhstudio.isettings.quicksettings.iapp.extension.loadInterAd
import com.nhstudio.isettings.quicksettings.iapp.extension.setFullScreen
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClick
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClickAlphaItemView


class PermissionFragment : Fragment() {


    private val binding by lazy { FragmentUserBinding.inflate(layoutInflater) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadBannerAdmob()
        setOnClick()
        setupColor()
    }

    private fun setupColor() {
        binding.apply {
            if(darkMode){
             binding.rootUser.setBackgroundColor(Color.BLACK)
            }
            rlOverlay.setPreventDoubleClick {
                goToIntent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                rlAllFile.setPreventDoubleClick {
                    goToIntent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                }
            } else {
                rlAllFile.beGone()
            }
            rlHome.setPreventDoubleClick {
                goToIntent(Settings.ACTION_HOME_SETTINGS)

            }

            rlDefaultApp.setPreventDoubleClick {
                goToIntent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                rlAlarm.setPreventDoubleClick {
                    goToIntent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                }
            } else {
                rlAlarm.beGone()
            }

            rlUnknown.setPreventDoubleClick {
                goToIntent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            }

            rlModifySystem.setPreventDoubleClick {
                goToIntent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            }

            rlNotification.setPreventDoubleClick {
                goToIntent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            }

        }
    }

    private fun goToIntent(intent: String) {
        try {
            canShowOpenAds = true
            startActivity(Intent(intent))
        } catch (e: Exception) {
            Toast.makeText(
                context,
                getString(R.string.your_device_does_not_support_this_feature), Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setOnClick() {
        binding.apply {

            rlTop.setPreventDoubleClickAlphaItemView {
                if (loadInterAd && config!!.pu) {
                    (activity as MainActivity).showDialogAd()
                    Handler(Looper.getMainLooper()).postDelayed({
                        (activity as MainActivity).showInter()
                        Handler(Looper.getMainLooper()).postDelayed({
                            findNavController().popBackStack()
                        }, 110)
                    }, 400)
                } else {
                    findNavController().popBackStack()
                }
            }

        }
    }





    override fun onResume() {
        super.onResume()
        canShowOpenAds = true
        activity?.setFullScreen()

    }

    private val adSize: AdSize?
        get() {
            val windowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.layoutAds.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return context?.let {
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                    it,
                    adWidth
                )
            }
        }


    var mAdViewAdmob: AdView? = null
    private fun loadBannerAdmob() {
        if (context?.config!!.pu && binding.layoutAds.haveInternet() ) {

            mAdViewAdmob = context?.let { AdView(it) }

            if (adSize != null) {
                mAdViewAdmob!!.setAdSize(adSize!!)
                binding.layoutAds.let { viewG ->
                    val lp = viewG.layoutParams
                    lp.width = adSize?.getWidthInPixels(viewG.context) ?: 0
                    lp.height = adSize?.getHeightInPixels(viewG.context) ?: 0
                    viewG.layoutParams = lp
                }
            } else {
                mAdViewAdmob!!.setAdSize(AdSize.BANNER)
            }
            if (isTesting) {
                mAdViewAdmob!!.adUnitId = "ca-app-pub-3940256099942544/6300978111"
            } else {
                mAdViewAdmob!!.adUnitId = ""
            }
            mAdViewAdmob?.setBackgroundColor(Color.WHITE)
            val adRequest = AdRequest.Builder().build()
            mAdViewAdmob!!.loadAd(adRequest)
            mAdViewAdmob!!.adListener = object : AdListener() {
                override fun onAdClicked() {
                    checkInter = true
                }

                override fun onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // Code to be executed when an ad request fails.
                    binding.layoutAds.beGone()
                }

                override fun onAdImpression() {
                    // Code to be executed when an impression is recorded
                    // for an ad.
                }

                override fun onAdLoaded() {
                    binding.layoutAds.removeAllViews()
                    binding.layoutAds.addView(mAdViewAdmob)
                    // Code to be executed when an ad finishes loading.
                }

                override fun onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }
            }
        } else {

            binding.layoutAds.beGone()
        }
    }
}