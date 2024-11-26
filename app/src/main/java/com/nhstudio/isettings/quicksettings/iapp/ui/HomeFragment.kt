package com.nhstudio.isettings.quicksettings.iapp.ui

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.add
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.MobileAds
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.nhstudio.iapp.appmanager.R
import com.nhstudio.iapp.appmanager.databinding.FragmentHomeBinding
import com.nhstudio.isettings.quicksettings.iapp.MainActivity
import com.nhstudio.isettings.quicksettings.iapp.adapter.AppListAdapter
import com.nhstudio.isettings.quicksettings.iapp.extension.LoadAppUtils
import com.nhstudio.isettings.quicksettings.iapp.extension.beGone
import com.nhstudio.isettings.quicksettings.iapp.extension.canShowOpenAds
import com.nhstudio.isettings.quicksettings.iapp.extension.config
import com.nhstudio.isettings.quicksettings.iapp.extension.darkMode
import com.nhstudio.isettings.quicksettings.iapp.extension.defaultSortList
import com.nhstudio.isettings.quicksettings.iapp.extension.loadInterAd
import com.nhstudio.isettings.quicksettings.iapp.extension.setFullScreen
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClick
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClickAlphaItemView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.addAll
import kotlin.text.firstOrNull
import kotlin.text.toUpperCase


class HomeFragment : Fragment() {

    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (findNavController().currentDestination!!.id == R.id.homeFragment) {
                findNavController().navigate(R.id.action_homeFragment_to_rateFragment)
            }
        }

        setOnClickListener()
        getAllApp()

    }

    private fun getAllApp() {
        if (defaultSortList.isEmpty()) {
            LoadAppUtils.getAppsAll {
            Log.i("dasdasdasdasdasdsa","vao1")
                binding.loadingView.beGone()
                defaultSortList.clear()
                defaultSortList.addAll(it.sortedBy { item ->
                    if (checkSelect(item)) {
                        -1
                    } else 1
                })
                initRvApp()

            }
        } else {
            Log.i("dasdasdasdasdasdsa","vao2")
//            initRvApp()
        }
    }

    private fun initRvApp() {
        CoroutineScope(Dispatchers.IO).launch {
            context?.let {
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
                    (binding.recyclerView.adapter as AppListAdapter).submitList(appListItems)
                }
            }


        }
    }



    fun groupAppsAlphabetically(
        appList: List<ApplicationInfo>,
        packageManager: PackageManager
    ): Map<Char, List<ApplicationInfo>> {
        return appList.groupBy {
            it.loadLabel(packageManager).toString().firstOrNull()?.toUpperCase() ?: '#'
        }
    }

    data class AppGroup(val letter: Char, val apps: List<ApplicationInfo>)

    private val listLockSelect: MutableList<String> = mutableListOf()
    private fun checkSelect(packageInfo: ApplicationInfo): Boolean {
        return listLockSelect.find { it == packageInfo.packageName } != null
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

    private fun setOnClickListener() {
        binding.apply {
            rlApp.setPreventDoubleClick {
                goToIntent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS)
            }
            viewSearchNext.setPreventDoubleClick {
                if (loadInterAd && config!!.pu) {
                    (activity as MainActivity).showDialogAd()
                    Handler(Looper.getMainLooper()).postDelayed({
                        (activity as MainActivity).showInter()
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (findNavController().currentDestination!!.id == R.id.homeFragment) {
                                findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
                            }
                        }, 110)
                    }, 400)
                } else {
                    if (findNavController().currentDestination!!.id == R.id.homeFragment) {
                        findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
                    }
                }

            }

            rlDevelop.setPreventDoubleClick {
                if (loadInterAd && config!!.pu) {
                    (activity as MainActivity).showDialogAd()
                    Handler(Looper.getMainLooper()).postDelayed({
                        (activity as MainActivity).showInter()
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (findNavController().currentDestination!!.id == R.id.homeFragment) {
                                findNavController().navigate(R.id.action_homeFragment_to_userFragment)
                            }
                        }, 110)
                    }, 400)
                } else {
                    if (findNavController().currentDestination!!.id == R.id.homeFragment) {
                        findNavController().navigate(R.id.action_homeFragment_to_userFragment)
                    }
                }

            }
            rlSystem.setPreventDoubleClick {
                if (loadInterAd && config!!.pu) {
                    (activity as MainActivity).showDialogAd()
                    Handler(Looper.getMainLooper()).postDelayed({
                        (activity as MainActivity).showInter()
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (findNavController().currentDestination!!.id == R.id.homeFragment) {
                                findNavController().navigate(R.id.action_homeFragment_to_systemFragment)
                            }
                        }, 110)
                    }, 400)
                } else {
                    if (findNavController().currentDestination!!.id == R.id.homeFragment) {
                        findNavController().navigate(R.id.action_homeFragment_to_systemFragment)
                    }
                }

            }


        }
    }


    override fun onResume() {
        super.onResume()
        activity?.setFullScreen()
        binding.isLight = !darkMode
    }
}

