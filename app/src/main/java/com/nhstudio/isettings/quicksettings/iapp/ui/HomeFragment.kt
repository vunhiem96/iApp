package com.nhstudio.isettings.quicksettings.iapp.ui

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.text.firstOrNull


class HomeFragment : Fragment() {

    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }


//    private var _binding: FragmentHomeBinding? = null
//    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
//        _binding = FragmentHomeBinding.inflate(inflater, container, false)
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
        binding.isLight = !darkMode
        if (defaultSortList.isEmpty()) {
            LoadAppUtils.getAppsAll {
                binding.loadingView.beGone()
                defaultSortList.clear()
                defaultSortList.addAll(it)
                initRvApp()
            }
        } else {
            binding.loadingView.beGone()
            initRvApp()
        }
    }

    private fun initRvApp() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val pm = context?.packageManager ?: return@launch
            val sorted = defaultSortList.sortedBy { item ->
                if (checkSelect(item)) -1 else 1
            }
            val groupedApps = groupAppsAlphabetically(sorted)
            val appListItems = mutableListOf<AppListAdapter.AppListItem>()
            for ((letter, apps) in groupedApps) {
                appListItems.add(AppListAdapter.AppListItem.LetterItem(letter))
                apps.forEachIndexed { index, appInfo ->
                    val label = LoadAppUtils.getAppName(appInfo)
                    val icon = appInfo.loadIcon(pm)
                    appListItems.add(
                        AppListAdapter.AppListItem.AppItem(
                            appInfo = appInfo,
                            label = label,
                            icon = icon,
                            isFirst = index == 0,
                            isLast = index == apps.size - 1
                        )
                    )
                }
            }
            withContext(Dispatchers.Main) {
                binding.recyclerView.adapter = AppListAdapter(pm)
                binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                (binding.recyclerView.adapter as AppListAdapter).submitList(appListItems)
            }
        }
    }

    private fun groupAppsAlphabetically(
        appList: List<ApplicationInfo>
    ): Map<Char, List<ApplicationInfo>> {
        return appList.groupBy {
            LoadAppUtils.getAppName(it).firstOrNull()?.uppercaseChar() ?: '#'
        }
    }

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

            rlZoom.setPreventDoubleClick {
                if (loadInterAd && config!!.pu) {
                    (activity as MainActivity).showDialogAd()
                    Handler(Looper.getMainLooper()).postDelayed({
                        (activity as MainActivity).showInter()
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (findNavController().currentDestination!!.id == R.id.homeFragment) {
                                findNavController().navigate(R.id.action_homeFragment_to_bigIconFragment)
                            }
                        }, 110)
                    }, 400)
                } else {
                    if (findNavController().currentDestination!!.id == R.id.homeFragment) {
                        findNavController().navigate(R.id.action_homeFragment_to_bigIconFragment)
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

