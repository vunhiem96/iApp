package com.nhstudio.isettings.quicksettings.iapp.ui

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
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
import com.nhstudio.isettings.quicksettings.iapp.MainActivity
import com.nhstudio.isettings.quicksettings.iapp.adapter.AppListAdapter
import com.nhstudio.isettings.quicksettings.iapp.extension.LoadAppUtils
import com.nhstudio.isettings.quicksettings.iapp.extension.beGone
import com.nhstudio.isettings.quicksettings.iapp.extension.canShowOpenAds
import com.nhstudio.isettings.quicksettings.iapp.extension.checkInter
import com.nhstudio.isettings.quicksettings.iapp.extension.config
import com.nhstudio.isettings.quicksettings.iapp.extension.darkMode
import com.nhstudio.isettings.quicksettings.iapp.extension.haveInternet
import com.nhstudio.isettings.quicksettings.iapp.extension.isTesting
import com.nhstudio.isettings.quicksettings.iapp.extension.loadInterAd
import com.nhstudio.isettings.quicksettings.iapp.extension.setFullScreen
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClickAlphaItemView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SystemFragment : Fragment() {
//    private val binding by lazy { FragmentSystemBinding.inflate(layoutInflater) }

    private var _binding: FragmentSystemBinding? = null
    private val binding get() = _binding!!
    private var cachedAppListItems: List<AppListAdapter.AppListItem>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSystemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.isLight = !darkMode
        loadBannerAdmob()
        setOnClick()
        initRvApp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initRvApp() {
        val cached = cachedAppListItems
        if (cached != null) {
            bindRecycler(cached)
            return
        }
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val ctx = context ?: return@launch
            defaultSortList = getAllSystemApps(ctx).toMutableList()
            val groupedApps = groupAppsAlphabetically(defaultSortList)
            val appListItems = mutableListOf<AppListAdapter.AppListItem>()
            for ((letter, apps) in groupedApps) {
                appListItems.add(AppListAdapter.AppListItem.LetterItem(letter))
                apps.forEachIndexed { index, appInfo ->
                    appListItems.add(
                        AppListAdapter.AppListItem.AppItem(
                            appInfo = appInfo,
                            label = LoadAppUtils.getAppName(appInfo),
                            isFirst = index == 0,
                            isLast = index == apps.size - 1
                        )
                    )
                }
            }
            cachedAppListItems = appListItems
            withContext(Dispatchers.Main) {
                if (_binding != null) {
                    bindRecycler(appListItems)
                }
            }
        }
    }

    private fun bindRecycler(appListItems: List<AppListAdapter.AppListItem>) {
        val ctx = context ?: return
        val adapter = AppListAdapter(ctx.packageManager) { packageName ->
            openAppSettings(packageName)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(ctx)
            setHasFixedSize(true)
            itemAnimator = null
            setItemViewCacheSize(20)
            this.adapter = adapter
        }
        adapter.submitList(appListItems)
        binding.loadingView.beGone()
    }

    private fun openAppSettings(packageName: String) {
        canShowOpenAds = true
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        } catch (_: Exception) {
            // ignore
        }
    }

    fun groupAppsAlphabetically(
        appList: List<ApplicationInfo>
    ): Map<Char, List<ApplicationInfo>> {
        return appList.groupBy {
            LoadAppUtils.getAppName(it).firstOrNull()?.uppercaseChar() ?: '#'
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