package com.nhstudio.isettings.quicksettings.iapp.extension

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import com.nhstudio.iapp.appmanager.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


object LoadAppUtils {
    private var packetManager: PackageManager? = null
    private var listApp: MutableList<ApplicationInfo> = mutableListOf()
    private var hashMap: HashMap<String, String?> = hashMapOf()
    private var listCallback: MutableList<(List<ApplicationInfo>) -> Unit> = mutableListOf()
    private var isRunning: Boolean = false
    fun init(context: Context?) {
        packetManager = context?.packageManager
    }

    fun getAppsAll(onSuccess: (List<ApplicationInfo>) -> Unit) {
        listCallback.add(onSuccess)
        Log.d("HUUIYYYIUIYUI", "${isRunning}")
        if (!isRunning) {
            isRunning = true
            CoroutineScope(Dispatchers.Default).launch {
                val pkgAppsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packetManager?.queryIntentActivities(
                        Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER),
                        PackageManager.ResolveInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                    )
                } else {
                    packetManager?.queryIntentActivities(
                        Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER),
                        PackageManager.GET_META_DATA
                    )
                }

                if (pkgAppsList != null) {
                    val list = pkgAppsList.mapNotNull { it.activityInfo.applicationInfo }
                        .filter { it.packageName != BuildConfig.APPLICATION_ID }
                        .distinctBy { it.packageName }
                        .toMutableList()
                    list.sortBy { getAppName(it).lowercase() }
                    listApp.clear()
                    listApp.addAll(list.toList())
                    list.find { it.packageName == BuildConfig.APPLICATION_ID }?.let {
                        list.remove(it)
                    }
                    withContext(Dispatchers.Main) {
                        Log.d("HUUIYYYIUIYUI", "${list.size}")
                        listCallback.forEach {
                            kotlin.runCatching {
                                it.invoke(list)
                            }.onFailure {
                                Log.d("HUUIYYYIUIYUI", "${it.message}")
                                it.printStackTrace()
                            }
                        }
                        listCallback.clear()
//                        onSuccess(list)
                    }
                    isRunning = false
                }
            }
        }
    }

    fun getListAllApps() = listApp.toList()
    fun getAppName(packageInfo: ApplicationInfo): String {
        var lab = hashMap[packageInfo.packageName]
        if (lab != null) return lab
        lab = packetManager?.let {
            packageInfo.loadLabel(it).toString()
        }
        hashMap[packageInfo.packageName] = lab
        return lab ?: ""
    }

    fun getAppName(packageInfo: ApplicationInfo, onSuccess: (String) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            val lab = hashMap[packageInfo.packageName]
            if (lab != null) {
                withContext(Dispatchers.Main) {
                    onSuccess(lab)
                }
            }
            val labNew = packetManager?.let {
                packageInfo.loadLabel(it).toString()
            }
            hashMap[packageInfo.packageName] = labNew
            if (labNew != null) {
                withContext(Dispatchers.Main) {
                    onSuccess(labNew)
                }
            }
        }
    }

    fun getIconApp(packageInfo: ApplicationInfo, onSuccess: (Drawable) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            packageInfo.loadIcon(packetManager)?.let {
                withContext(Dispatchers.Main) {
                    onSuccess(it)
                }
            }
        }
    }
}