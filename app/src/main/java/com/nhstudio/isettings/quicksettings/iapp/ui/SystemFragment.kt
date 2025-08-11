package com.nhstudio.isettings.quicksettings.iapp.ui

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.nhstudio.iapp.appmanager.R
import com.nhstudio.iapp.appmanager.databinding.FragmentSystemBinding
import com.nhstudio.iapp.appmanager.databinding.FragmentUserBinding
import com.nhstudio.isettings.quicksettings.iapp.MainActivity
import com.nhstudio.isettings.quicksettings.iapp.adapter.AppListAdapter
import com.nhstudio.isettings.quicksettings.iapp.extension.beGone
import com.nhstudio.isettings.quicksettings.iapp.extension.canShowOpenAds
import com.nhstudio.isettings.quicksettings.iapp.extension.checkInter
import com.nhstudio.isettings.quicksettings.iapp.extension.config
import com.nhstudio.isettings.quicksettings.iapp.extension.darkMode
import com.nhstudio.isettings.quicksettings.iapp.extension.gone
import com.nhstudio.isettings.quicksettings.iapp.extension.haveInternet
import com.nhstudio.isettings.quicksettings.iapp.extension.isTesting
import com.nhstudio.isettings.quicksettings.iapp.extension.loadInterAd
import com.nhstudio.isettings.quicksettings.iapp.extension.setFullScreen
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClickAlphaItemView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SystemFragment : Fragment() {
    private val binding by lazy { FragmentSystemBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.isLight = !darkMode
        loadBannerAdmob()
        setOnClick()
        initRvApp()
    }

    private fun initRvApp() {
        CoroutineScope(Dispatchers.IO).launch {
            context?.let {
                defaultSortList = getAllSystemApps(it).toMutableList()
                val groupedApps = groupAppsAlphabetically(
                   defaultSortList,
                    packageManager = it.packageManager
                )
                val appListItems = mutableListOf<AppListAdapter.AppListItem>()
                for ((letter, apps) in groupedApps) {
                    appListItems.add(AppListAdapter.AppListItem.LetterItem(letter))
                    apps.forEachIndexed { index, appInfo ->
                        val isFirst = index == 0
                        val isLast = index == apps.size - 1
                        appListItems.add(
                            AppListAdapter.AppListItem.AppItem(
                                appInfo,
                                isFirst = isFirst,
                                isLast = isLast
                            )
                        )
                    }
                }
                withContext(Dispatchers.Main) {
                    binding.recyclerView.adapter = AppListAdapter(it.packageManager)
                    binding.recyclerView.layoutManager = LinearLayoutManager(it)
                    binding.recyclerView.setItemViewCacheSize(300)
                    (binding.recyclerView.adapter as AppListAdapter).submitList(appListItems)
                    binding.loadingView.beGone()
                }
            }


        }
    }

    fun groupAppsAlphabetically(
        appList: List<ApplicationInfo>,
        packageManager: PackageManager
    ): Map<Char, List<ApplicationInfo>> {
        return appList.groupBy {
            it.loadLabel(packageManager).toString().firstOrNull()?.uppercaseChar() ?: '#'
        }
    }
    var defaultSortList: MutableList<ApplicationInfo> = mutableListOf()

    fun getAllSystemApps(context: Context): List<ApplicationInfo> {
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        return packages.filter { it.flags and ApplicationInfo.FLAG_SYSTEM != 0 }
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
                mAdViewAdmob!!.adUnitId = "ca-app-pub-9589105932398084/2828828028"
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