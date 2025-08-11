package com.nhstudio.isettings.quicksettings.iapp.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.launch
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.nhstudio.iapp.appmanager.R
import com.nhstudio.iapp.appmanager.databinding.FragmentRateBinding
import com.nhstudio.isettings.quicksettings.iapp.MainActivity
import com.nhstudio.isettings.quicksettings.iapp.extension.beGone
import com.nhstudio.isettings.quicksettings.iapp.extension.beInvisible
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
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClickScaleView
import com.nhstudio.isettings.quicksettings.iapp.extension.showInterOk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess


class RateFragment : Fragment() {

    private val binding by lazy { FragmentRateBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.isLight = !darkMode
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finish()
            exitProcess(0)
        }
        setOnClick()
        loadBannerAdmob()

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
            val extras = Bundle()
            extras.putString("collapsible", "bottom")

            val adRequest = AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                .build()
            if (isTesting) {
                mAdViewAdmob!!.adUnitId = "ca-app-pub-3940256099942544/2014213617"
            } else {
                mAdViewAdmob!!.adUnitId = "ca-app-pub-9589105932398084/2828828028"
            }
            mAdViewAdmob?.setBackgroundColor(Color.WHITE)
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
                    binding.layoutAds.beInvisible()
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
            binding.layoutAds.beInvisible()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mAdViewAdmob?.destroy()
    }
    private fun setOnClick() {
        binding.apply {
            if (!config!!.pu) {
                rlRatePro.beGone()
            }
            rlGomain.setPreventDoubleClick(500) {
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
            rlGoSt.setPreventDoubleClick(500) {
                if (loadInterAd && config!!.pu) {
                    (activity as MainActivity).showDialogAd()
                    Handler(Looper.getMainLooper()).postDelayed({
                        (activity as MainActivity).showInter()
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (findNavController().currentDestination!!.id == R.id.rateFragment) findNavController().navigate(
                                R.id.action_rateFragment_to_settingFragment
                            )
                        }, 110)
                    }, 400)
                } else {
                    if (findNavController().currentDestination!!.id == R.id.rateFragment) findNavController().navigate(
                        R.id.action_rateFragment_to_settingFragment
                    )
                }


            }

            rlRatePro.setPreventDoubleClick(500) {
                if (loadInterAd && config!!.pu) {
                    (activity as MainActivity).showDialogAd()
                    Handler(Looper.getMainLooper()).postDelayed({
                        (activity as MainActivity).showInter()
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (findNavController().currentDestination!!.id == R.id.rateFragment) {
                                findNavController().navigate(R.id.action_rateFragment_to_iapFragment)
                            }
                        }, 110)
                    }, 400)
                } else {
                    if (findNavController().currentDestination!!.id == R.id.rateFragment) {
                        findNavController().navigate(R.id.action_rateFragment_to_iapFragment)
                    }
                }
            }


            btnYes22.setPreventDoubleClickAlphaItemView(500) {
                activity?.apply {
                    if (isChoseStar) {
                        config.rateApp = true
                        if (isFiveStar) {
                            gotoMarket()
                            Toast.makeText(
                                this,
                                getString(R.string.rate_5start),
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.rate_4start),
                                Toast.LENGTH_LONG
                            ).show()
                            Handler().postDelayed({
                                finish()
                            }, 400)

                        }
                    } else {
                        Toast.makeText(
                            this, getString(R.string.no_star),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            }

            btnNo2.setPreventDoubleClickAlphaItemView(500) {
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
            btnNo22.setPreventDoubleClickAlphaItemView(500) {
                exitProcess(0)
            }

            btnYes2.setPreventDoubleClickAlphaItemView(500) {
                exitProcess(0)

            }
        }
    }

    private fun gotoMarket() {
        val uri = Uri.parse("market://details?id=" + activity?.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + activity?.packageName)
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        checkInter = false
        setUpRate()
        activity?.setFullScreen()
        canShowOpenAds = true
        if(!showInterOk){
            checkInter = true

        }
    }

    private fun setUpRate() {
        binding.apply {
            activity.let {

                if (it!!.config.rateApp == false) {
                    llRate.visibility = View.VISIBLE
                    clickRate()
                } else {
                    llExit.visibility = View.VISIBLE
                }
            }

        }

    }

    private var isFiveStar = false
    private var isChoseStar = false
    private fun clickRate() {
        binding.apply {
            oneStar.setPreventDoubleClickScaleView(500) {
                isChoseStar = true
                isFiveStar = false
                oneStar.setImageResource(R.drawable.star_checked)
                twoStar.setImageResource(R.drawable.ic_star_off)
                threeStar.setImageResource(R.drawable.ic_star_off)
                fourStar.setImageResource(R.drawable.ic_star_off)
                fiveStar.setImageResource(R.drawable.ic_star_off)
            }
            twoStar.setPreventDoubleClickScaleView(500) {
                isChoseStar = true
                isFiveStar = false
                oneStar.setImageResource(R.drawable.star_checked)
                twoStar.setImageResource(R.drawable.star_checked)
                threeStar.setImageResource(R.drawable.ic_star_off)
                fourStar.setImageResource(R.drawable.ic_star_off)
                fiveStar.setImageResource(R.drawable.ic_star_off)
            }
            threeStar.setPreventDoubleClickScaleView(500) {
                isChoseStar = true
                isFiveStar = false
                oneStar.setImageResource(R.drawable.star_checked)
                twoStar.setImageResource(R.drawable.star_checked)
                threeStar.setImageResource(R.drawable.star_checked)
                fourStar.setImageResource(R.drawable.ic_star_off)
                fiveStar.setImageResource(R.drawable.ic_star_off)
            }
            fourStar.setPreventDoubleClickScaleView(500) {
                isChoseStar = true
                isFiveStar = false
                oneStar.setImageResource(R.drawable.star_checked)
                twoStar.setImageResource(R.drawable.star_checked)
                threeStar.setImageResource(R.drawable.star_checked)
                fourStar.setImageResource(R.drawable.star_checked)
                fiveStar.setImageResource(R.drawable.ic_star_off)
            }
            fiveStar.setPreventDoubleClickScaleView(500) {
                isChoseStar = true
                isFiveStar = true
                oneStar.setImageResource(R.drawable.star_checked)
                twoStar.setImageResource(R.drawable.star_checked)
                threeStar.setImageResource(R.drawable.star_checked)
                fourStar.setImageResource(R.drawable.star_checked)
                fiveStar.setImageResource(R.drawable.star_checked)
            }
        }
    }
    private fun getAllApp(){
        lifecycleScope.launch {
            val appList = withContext(Dispatchers.IO) {
                context?.let { getAllInstalledApps(it) } // Truyền Context của Activity
            }

            withContext(Dispatchers.Main) {
                Log.i("asdasdasdasdqweqwewqe","${appList!!.size}")
            }
        }


    }
    fun getAllInstalledApps(context: Context): List<AppInfo> {
        val packageManager = context.packageManager
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps.map { app ->
            AppInfo(
                name = app.loadLabel(packageManager).toString(),
                packageName = app.packageName,
                icon = app.loadIcon(packageManager)
            )
        }
    }

    data class AppInfo(
        val name: String,
        val packageName: String,
        val icon: Drawable
    )


}