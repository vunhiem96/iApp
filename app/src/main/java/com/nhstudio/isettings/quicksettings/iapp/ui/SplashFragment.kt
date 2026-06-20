package com.nhstudio.isettings.quicksettings.iapp.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.nhstudio.iapp.appmanager.BuildConfig
import com.nhstudio.iapp.appmanager.R
import com.nhstudio.iapp.appmanager.databinding.FragmentSplashBinding
import com.nhstudio.isettings.quicksettings.iapp.MainActivity
import com.nhstudio.isettings.quicksettings.iapp.cmp.CMPCallback
import com.nhstudio.isettings.quicksettings.iapp.cmp.CMPController
import com.nhstudio.isettings.quicksettings.iapp.extension.beVisible
import com.nhstudio.isettings.quicksettings.iapp.extension.config
import com.nhstudio.isettings.quicksettings.iapp.extension.haveInternet
import com.nhstudio.isettings.quicksettings.iapp.extension.isTesting
import com.nhstudio.isettings.quicksettings.iapp.extension.loadInterSplash
import com.nhstudio.isettings.quicksettings.iapp.extension.onScreen
import com.nhstudio.isettings.quicksettings.iapp.extension.onScreenCancel
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClickAlphaItemView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }


    private var mInterstitialAd: InterstitialAd? = null
    private var pendingAdShow = false
    private var isTimeOut = false

    private val handler = Handler(Looper.getMainLooper())
    private val timeoutMillis = 15_000L
    private var clickNext = false

    private val timeoutRunnable = Runnable {
        if (_binding != null) {
            binding.btnNext.beVisible()
            binding.tvLoading.text = getString(R.string.loading_initial_data3)
        }
        isTimeOut = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            Handler(Looper.getMainLooper()).postDelayed({
                it.config.isFO = false
            },2000)
            it.onScreen()
//            Log.i("dasdasdasdasdasdas","vao1")
//            binding.progressBar.startAnim(10000) {}
            CMPController(it).showCMP(
                BuildConfig.DEBUG,
                object : CMPCallback {
                    override fun onShowAd() {
//                        Log.i("testInterSplash", "dong y cpm")
                        loadInterstitialAd()


                    }

                    override fun onChangeScreen() {
                        loadInterstitialAd()

                    }
                })
        }

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {

        }
        binding.btnNext.setPreventDoubleClickAlphaItemView {
            if (mInterstitialAd != null && !clickNext) {
                clickNext = true
                showInterstitial()
            } else {
                if (findNavController().currentDestination?.id == R.id.splashFragment) {
                    findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                }
            }
        }
    }

    var idAdmob = "ca-app-pub-9589105932398084/6393004175"
    private fun loadInterstitialAd() {
        val binding = _binding ?: return
        if (binding.tvLoading.haveInternet()) {
            if (isTesting) {
                idAdmob = "ca-app-pub-3940256099942544/1033173712"
            }
            if (loadInterSplash) return
            setUpText()
            binding.progressBar.startAnim(15000) {
                if (_binding != null) {
                    binding.btnNext.beVisible()
                    binding.tvLoading.text = getString(R.string.loading_initial_data3)
                }
            }
            handler.postDelayed(timeoutRunnable, timeoutMillis)
            val adRequest = AdRequest.Builder().build()
            loadInterSplash = true
            InterstitialAd.load(
                requireContext(),
                idAdmob,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
//                    Log.i("testInterSplash", "load thanh cong")
                        mInterstitialAd = interstitialAd
                        // Nếu App đang hiển thị (Resumed) thì show luôn
                        if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            showInterstitial()
//                        Log.i("testInterSplash", "goi show")
                        } else {
                            // Nếu App đang ở background, đánh dấu để show khi quay lại
                            pendingAdShow = true
//                        Log.i(
//                            "testInterSplash",
//                            "App đang ở background, đánh dấu để show khi quay lại"
//                        )
                        }
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        mInterstitialAd = null
                        goNext()
                    }
                }
            )
        } else {
            goNext()
        }
    }


    private fun setUpText() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (_binding != null) {
                binding.tvLoading.text = getString(R.string.loading_initial_data1)
            }
        }, 8000)

        Handler(Looper.getMainLooper()).postDelayed({
            if (_binding != null) {
                binding.tvLoading.text = getString(R.string.loading_initial_data2)
            }
        }, 12000)
    }

    private fun showInterstitial() {
        if (context?.config != null) {
            if (!context?.config!!.pu) {
                if (findNavController().currentDestination?.id == R.id.splashFragment) {
                    findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                }
                return
            }
        }
        val activity = activity ?: run { goNext(); return }
        val ad = mInterstitialAd ?: run { goNext(); return }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
//                Log.i("testInterSplash", "ads show")
                mInterstitialAd = null
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                goNext()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
//                Log.i("testInterSplash", "ads show fail")
                goNext()
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            ad.show(activity)
        }, 300)

    }

    private fun goNext() {
        if (_binding != null) {
            binding.btnNext.beVisible()
        }
        try {
            if (findNavController().currentDestination?.id == R.id.splashFragment) {
//                Log.i("testInterSplash", "nav home")
                (activity as? MainActivity)?.setupOpenAds()
                findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
            }
        } catch (e: Exception) {

        }
    }

    override fun onResume() {
        super.onResume()
        if (pendingAdShow && mInterstitialAd != null) {
            showInterstitial()
            pendingAdShow = false
        } else {
//            Log.i("testInterSplash", "time out")
            if (isTimeOut) {
                if (_binding != null) {
                    binding.btnNext.beVisible()
                    binding.tvLoading.text = getString(R.string.loading_initial_data3)
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        activity?.onScreenCancel()
        _binding = null
        handler.removeCallbacks(timeoutRunnable)
        mInterstitialAd?.fullScreenContentCallback = null
        mInterstitialAd = null
    }
}