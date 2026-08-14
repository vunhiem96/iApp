package com.nhstudio.isettings.quicksettings.iapp.extension

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.LruCache
import androidx.core.content.ContextCompat
import com.nhstudio.iapp.appmanager.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


object LoadAppUtils {
    private var packetManager: PackageManager? = null
    private var listApp: MutableList<ApplicationInfo> = mutableListOf()
    private var hashMap: HashMap<String, String?> = hashMapOf()
    private val iconCache = LruCache<String, Drawable>(120)
    private var listCallback: MutableList<(List<ApplicationInfo>) -> Unit> = mutableListOf()
    private var isRunning: Boolean = false
    private val appsChangedListeners = mutableListOf<() -> Unit>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var packageReceiverRegistered = false

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val packageName = intent.data?.schemeSpecificPart ?: return
            if (packageName == BuildConfig.APPLICATION_ID) return
            val replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
            when (intent.action) {
                Intent.ACTION_PACKAGE_REMOVED -> {
                    if (!replacing) {
                        removePackage(packageName)
                    }
                }
                Intent.ACTION_PACKAGE_ADDED -> {
                    if (!replacing) {
                        addPackage(packageName)
                    }
                }
                Intent.ACTION_PACKAGE_REPLACED -> {
                    hashMap.remove(packageName)
                    iconCache.remove(packageName)
                }
            }
        }
    }

    fun init(context: Context?) {
        packetManager = context?.packageManager
        val appContext = context?.applicationContext ?: return
        if (packageReceiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(
            appContext,
            packageReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
        packageReceiverRegistered = true
    }

    fun addAppsChangedListener(listener: () -> Unit) {
        if (!appsChangedListeners.contains(listener)) {
            appsChangedListeners.add(listener)
        }
    }

    fun removeAppsChangedListener(listener: () -> Unit) {
        appsChangedListeners.remove(listener)
    }

    fun removePackage(packageName: String) {
        val removedFromCache = listApp.removeAll { it.packageName == packageName }
        val removedFromDefault = defaultSortList.removeAll { it.packageName == packageName }
        hashMap.remove(packageName)
        iconCache.remove(packageName)
        if (removedFromCache || removedFromDefault) {
            notifyAppsChanged()
        }
    }

    private fun addPackage(packageName: String) {
        val pm = packetManager ?: return
        if (listApp.any { it.packageName == packageName }) return
        if (pm.getLaunchIntentForPackage(packageName) == null) return
        val appInfo = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, 0)
            }
        }.getOrNull() ?: return
        listApp.add(appInfo)
        listApp.sortBy { getAppName(it).lowercase() }
        defaultSortList.clear()
        defaultSortList.addAll(listApp)
        notifyAppsChanged()
    }

    private fun notifyAppsChanged() {
        mainHandler.post {
            appsChangedListeners.toList().forEach { listener ->
                runCatching { listener() }
            }
        }
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

                try {
                    if (pkgAppsList != null) {
                        val list = pkgAppsList.mapNotNull { it.activityInfo.applicationInfo }
                            .filter { it.packageName != BuildConfig.APPLICATION_ID }
                            .distinctBy { it.packageName }
                            .toMutableList()
                        list.sortBy { getAppName(it).lowercase() }
                        listApp.clear()
                        listApp.addAll(list.toList())
                        defaultSortList.clear()
                        defaultSortList.addAll(list)
                        withContext(Dispatchers.Main) {
                            listCallback.forEach { cb ->
                                kotlin.runCatching {
                                    cb.invoke(list)
                                }.onFailure {
                                    Log.d("HUUIYYYIUIYUI", "${it.message}")
                                    it.printStackTrace()
                                }
                            }
                            listCallback.clear()
                        }
                    }
                } finally {
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
                return@launch
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

    fun getCachedIcon(packageName: String): Drawable? = iconCache.get(packageName)

    fun getIconApp(packageInfo: ApplicationInfo, onSuccess: (Drawable) -> Unit) {
        val cached = iconCache.get(packageInfo.packageName)
        if (cached != null) {
            onSuccess(cached)
            return
        }
        CoroutineScope(Dispatchers.Default).launch {
            val pm = packetManager ?: return@launch
            val icon = packageInfo.loadIcon(pm) ?: return@launch
            iconCache.put(packageInfo.packageName, icon)
            withContext(Dispatchers.Main) {
                onSuccess(icon)
            }
        }
    }
}
