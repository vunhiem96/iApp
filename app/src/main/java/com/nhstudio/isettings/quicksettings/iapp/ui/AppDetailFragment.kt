package com.nhstudio.isettings.quicksettings.iapp.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.nhstudio.iapp.appmanager.R
import com.nhstudio.iapp.appmanager.databinding.FragmentAppDetailBinding
import com.nhstudio.isettings.quicksettings.iapp.extension.LoadAppUtils
import com.nhstudio.isettings.quicksettings.iapp.extension.beGone
import com.nhstudio.isettings.quicksettings.iapp.extension.canShowOpenAds
import com.nhstudio.isettings.quicksettings.iapp.extension.checkInter
import com.nhstudio.isettings.quicksettings.iapp.extension.config
import com.nhstudio.isettings.quicksettings.iapp.extension.darkMode
import com.nhstudio.isettings.quicksettings.iapp.extension.haveInternet
import com.nhstudio.isettings.quicksettings.iapp.extension.isTesting
import com.nhstudio.isettings.quicksettings.iapp.extension.setFullScreen
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClick
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClickAlphaItemView
import java.text.DateFormat
import java.util.Date

class AppDetailFragment : Fragment() {

    private var _binding: FragmentAppDetailBinding? = null
    private val binding get() = _binding!!

    private var packageNameArg: String = ""
    private var mAdViewAdmob: AdView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.isLight = !darkMode
        packageNameArg = arguments?.getString(ARG_PACKAGE_NAME).orEmpty()
        if (packageNameArg.isBlank()) {
            findNavController().popBackStack()
            return
        }
        bindAppInfo()
        setOnClick()
        loadBannerAdmob()
    }

    override fun onResume() {
        super.onResume()
        canShowOpenAds = true
        activity?.setFullScreen()
    }

    override fun onDestroyView() {
        mAdViewAdmob = null
        super.onDestroyView()
        _binding = null
    }

    private fun bindAppInfo() {
        val pm = requireContext().packageManager
        try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageNameArg, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageNameArg, 0)
            }
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageNameArg, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageNameArg, 0)
            }

            binding.tvAppName.text = LoadAppUtils.getAppName(appInfo).ifBlank {
                appInfo.loadLabel(pm).toString()
            }
            binding.tvPackageName.text = packageNameArg

            val cachedIcon = LoadAppUtils.getCachedIcon(packageNameArg)
            if (cachedIcon != null) {
                binding.appIconImageView.setImageDrawable(cachedIcon)
            } else {
                LoadAppUtils.getIconApp(appInfo) { icon ->
                    if (_binding != null) {
                        binding.appIconImageView.setImageDrawable(icon)
                    }
                }
            }

            binding.tvVersion.text = packageInfo.versionName ?: getString(R.string.unknown_value)
            binding.tvVersionCode.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }
            binding.tvInstallTime.text = formatTime(packageInfo.firstInstallTime)
            binding.tvUpdateTime.text = formatTime(packageInfo.lastUpdateTime)
            binding.tvTargetSdk.text = appInfo.targetSdkVersion.toString()
        } catch (_: Exception) {
            Toast.makeText(requireContext(), getString(R.string.app_not_found), Toast.LENGTH_SHORT)
                .show()
            findNavController().popBackStack()
        }
    }

    private fun formatTime(millis: Long): String {
        if (millis <= 0L) return getString(R.string.unknown_value)
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            .format(Date(millis))
    }

    private fun setOnClick() {
        binding.apply {
            rlTop.setPreventDoubleClickAlphaItemView {
                findNavController().popBackStack()
            }
            btnOpenApp.setPreventDoubleClickAlphaItemView {
                openApp()
            }
            btnOpenPlayStore.setPreventDoubleClickAlphaItemView {
                openGooglePlay()
            }
            btnOpenAppSettings.setPreventDoubleClickAlphaItemView {
                openAppSettings()
            }
        }
    }

    private fun openApp() {
        try {
            val intent = requireContext().packageManager.getLaunchIntentForPackage(packageNameArg)
            if (intent != null) {
                canShowOpenAds = true
                startActivity(intent)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.app_not_found),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), getString(R.string.app_not_found), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun openGooglePlay() {
        canShowOpenAds = true
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$packageNameArg")
                )
            )
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageNameArg")
                    )
                )
            } catch (_: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.your_device_does_not_support_this_feature),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openAppSettings() {
        canShowOpenAds = true
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageNameArg, null)
            }
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(
                requireContext(),
                getString(R.string.your_device_does_not_support_this_feature),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val adSize: AdSize?
        get() {
            val windowManager =
                requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
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
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(it, adWidth)
            }
        }

    private fun loadBannerAdmob() {
        if (context?.config!!.pu && binding.layoutAds.haveInternet()) {
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

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    binding.layoutAds.beGone()
                }

                override fun onAdLoaded() {
                    binding.layoutAds.removeAllViews()
                    binding.layoutAds.addView(mAdViewAdmob)
                }
            }
        } else {
            binding.layoutAds.beGone()
        }
    }

    companion object {
        const val ARG_PACKAGE_NAME = "packageName"
    }
}
