package com.nhstudio.isettings.quicksettings.iapp.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.nhstudio.iapp.appmanager.R
import com.nhstudio.iapp.appmanager.databinding.FragmentSettingBinding
import com.nhstudio.isettings.quicksettings.iapp.MainActivity
import com.nhstudio.isettings.quicksettings.iapp.dialog.ConfirmationDialog
import com.nhstudio.isettings.quicksettings.iapp.extension.PhotorTool
import com.nhstudio.isettings.quicksettings.iapp.extension.beGone
import com.nhstudio.isettings.quicksettings.iapp.extension.config
import com.nhstudio.isettings.quicksettings.iapp.extension.darkMode
import com.nhstudio.isettings.quicksettings.iapp.extension.loadInterAd
import com.nhstudio.isettings.quicksettings.iapp.extension.setFullScreen
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClick
import java.util.Calendar


class SettingFragment : Fragment() {
    private val binding by lazy { FragmentSettingBinding.inflate(layoutInflater) }
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
        setUpFullScreen()
        darkMode()
        onClick()
        loadImage()
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
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

    private fun loadImage() {
        context?.let {
            Glide.with(it)
                .load("https://i.pinimg.com/originals/07/76/af/0776afad4f8e14f4a8578ffbcc55f7bc.jpg")
                .into(binding.card1)
        }
        context?.let {
            Glide.with(it)
                .load("https://i.pinimg.com/originals/23/ad/c2/23adc269cb5a82b33c09537e6a4c72a2.jpg")
                .into(binding.card2)
        }
        context?.let {
            Glide.with(it)
                .load("https://i.pinimg.com/originals/68/65/95/686595f682b734a3c2782c45dd34da17.jpg")
                .into(binding.card3)
        }
        context?.let {
            Glide.with(it)
                .load("https://i.pinimg.com/originals/d0/5d/ab/d05dabc7f1dec7a49ebb02ccf6da4bf4.jpg")
                .into(binding.card4)
        }
    }

    private fun darkMode() {
        binding.apply {
            context?.let {
                val config = it.config
                if (config.darkMode == 0) {
                    darkOn.isChecked = true
                } else if (config.darkMode == 1) {
                    lightOn.isChecked = true
                } else {
                    autoOn.isChecked = true
                }
                tvDark.setOnClickListener {
                    darkOn.isChecked = true
                    config.darkMode = 0
                    setUpColor()

                }
                tvLight.setOnClickListener {
                    lightOn.isChecked = true
                    config.darkMode = 1
                    setUpColor()

                }
                tvAuto.setOnClickListener {
                    autoOn.isChecked = true
                    config.darkMode = 2
                    setUpColor()

                }
                darkOn.setOnClickListener {
                    config.darkMode = 0
                    setUpColor()
                }
                lightOn.setOnClickListener {
                    config.darkMode = 1
                    setUpColor()

                }
                autoOn.setOnClickListener {
                    config.darkMode = 2
                    setUpColor()
                }
            }

        }
    }

    private fun onClick() {
        binding.apply {
            if(!config!!.pu){
                binding.adsAll.beGone()
                binding.textPro.text = "Pro Version"
            }
            adsAll.setPreventDoubleClick(500) {
                ConfirmationDialog(
                    requireActivity(),
                    "Open Google Play to download app?"
                ) {
                    newApp("N-HStudio")
                }.show()

            }
            llBack.setPreventDoubleClick(500) {
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

            iapClick.setPreventDoubleClick(500) {
                if(config!!.pu) {
                    if (loadInterAd && config!!.pu) {
                        (activity as MainActivity).showDialogAd()
                        Handler(Looper.getMainLooper()).postDelayed({
                            (activity as MainActivity).showInter()
                            Handler(Looper.getMainLooper()).postDelayed({
                                if (findNavController().currentDestination!!.id == R.id.settingFragment) {
                                    findNavController().navigate(R.id.action_settingFragment_to_iapFragment)
                                }
                            }, 110)
                        }, 400)
                    } else {
                        if (findNavController().currentDestination!!.id == R.id.settingFragment) {
                            findNavController().navigate(R.id.action_settingFragment_to_iapFragment)
                        }
                    }
                }
            }
//        ads_all?.setPreventDoubleClick(500){
//            if(findNavController().currentDestination!!.id == R.id.settingFragment){
//                findNavController().navigate(R.id.action_settingFragment_to_myAdsFragment)
//            }
//        }
            privacyApp.setPreventDoubleClick(500) {
                (activity as MainActivity).resetIAP()
                val uri =
                    Uri.parse("https://sites.google.com/view/policyijournal/policy") // missing 'http://' will cause crashed
                val intent = Intent(Intent.ACTION_VIEW, uri)
                try {
                    startActivity(intent)
                } catch (_: Exception) {
                }
            }

            rlFeedback.setPreventDoubleClick(500) {
                var isPro = ""
                if (!requireContext().config.pu) {
                    isPro = "Pro Version"
                }
                PhotorTool.sendMail(
                    context, isPro
                )
            }
            share.setPreventDoubleClick(500) {
                context?.shareApp()
            }
            rateApp2.setPreventDoubleClick(500) {
                ConfirmationDialog(
                    requireActivity(),
                    "Open Google Play to download app?"
                ) {

                    newApp("N-HStudio")
                }.show()
            }
        }

    }

    fun newApp(idDeveloper: String) {
        val uri = Uri.parse("market://search?q=pub:$idDeveloper")
        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(myAppLinkToMarket)
        } catch (e: ActivityNotFoundException) {
        }
    }

    fun Context.shareApp() {
        val linkApk =
            "https://play.google.com/store/apps/details?id=" + applicationContext!!.packageName
        val appName = getString(R.string.app_name2)
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.run {
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name2))  // title
            putExtra(
                Intent.EXTRA_TEXT,
                "Download app " + getString(R.string.app_name2) + ": $linkApk"  // content
            )
        }
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)))
    }

    private fun toaskReset() {
        val toast =
            Toast.makeText(requireContext(), getString(R.string.reset_upate), Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.BOTTOM, 0, 210)
        toast.show()
    }

    private fun setUpColor() {
        context?.let {
            val config = it.config
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
        toaskReset()
    }

    override fun onResume() {
        super.onResume()
        activity?.setFullScreen()
    }

    private fun setUpFullScreen() {
        binding.apply {
            checkFullLight.isChecked = context?.config!!.setFullScreen
            checkFullDark.isChecked = context?.config!!.setFullScreen

            checkFullLight.setOnCheckedChangeListener { _, isChecked ->
                context?.config!!.setFullScreen = isChecked
                activity?.setFullScreen()

            }
            checkFullDark.setOnCheckedChangeListener { _, isChecked ->
                context?.config!!.setFullScreen = isChecked
                activity?.setFullScreen()
            }
        }

    }
}