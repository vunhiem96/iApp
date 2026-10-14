package com.nhstudio.isettings.quicksettings.iapp.extension

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.StringRes
import com.nhstudio.iapp.appmanager.R
import java.io.File
import java.text.Collator

object AppSort {
    const val NAME_AZ = 0
    const val NAME_ZA = 1
    const val INSTALL_TIME = 2
    const val SIZE_ASC = 3
    const val SIZE_DESC = 4
    const val INSTALL_TIME_OLD = 5

    private var installTimes: Map<String, Long>? = null

    @StringRes
    fun labelRes(mode: Int): Int {
        return when (mode) {
            NAME_ZA -> R.string.sort_name_za
            INSTALL_TIME -> R.string.sort_install_time
            INSTALL_TIME_OLD -> R.string.sort_install_time_old
            SIZE_ASC -> R.string.sort_size_asc
            SIZE_DESC -> R.string.sort_size_desc
            else -> R.string.sort_name_az
        }
    }

    fun usesLetterGroups(mode: Int): Boolean {
        return mode == NAME_AZ || mode == NAME_ZA
    }

    fun invalidateInstallTimes() {
        installTimes = null
    }

    fun sort(
        context: Context,
        apps: List<ApplicationInfo>,
        mode: Int = context.config.appSortMode
    ): List<ApplicationInfo> {
        if (apps.size < 2) return apps
        val collator = Collator.getInstance().apply { strength = Collator.PRIMARY }
        val nameOf: (ApplicationInfo) -> String = { LoadAppUtils.getAppName(it) }
        return when (mode) {
            NAME_ZA -> apps.sortedWith { left, right ->
                val byName = collator.compare(nameOf(right), nameOf(left))
                if (byName != 0) byName else left.packageName.compareTo(right.packageName)
            }
            INSTALL_TIME -> {
                val times = loadInstallTimes(context.packageManager)
                apps.sortedWith(
                    compareByDescending<ApplicationInfo> { times[it.packageName] ?: 0L }
                        .thenBy { nameOf(it) }
                )
            }
            INSTALL_TIME_OLD -> {
                val times = loadInstallTimes(context.packageManager)
                apps.sortedWith(
                    compareBy<ApplicationInfo> {
                        val time = times[it.packageName] ?: 0L
                        if (time <= 0L) Long.MAX_VALUE else time
                    }.thenBy { nameOf(it) }
                )
            }
            SIZE_ASC -> apps.sortedWith(
                compareBy<ApplicationInfo> { apkBytes(it) }.thenBy { nameOf(it) }
            )
            SIZE_DESC -> apps.sortedWith(
                compareByDescending<ApplicationInfo> { apkBytes(it) }.thenBy { nameOf(it) }
            )
            else -> apps.sortedWith { left, right ->
                val byName = collator.compare(nameOf(left), nameOf(right))
                if (byName != 0) byName else left.packageName.compareTo(right.packageName)
            }
        }
    }

    private fun loadInstallTimes(packageManager: PackageManager): Map<String, Long> {
        installTimes?.let { return it }
        val packages = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledPackages(0)
            }
        } catch (_: Exception) {
            emptyList()
        }
        val map = HashMap<String, Long>(packages.size)
        for (info in packages) {
            map[info.packageName] = info.firstInstallTime
        }
        installTimes = map
        return map
    }

    private fun apkBytes(info: ApplicationInfo): Long {
        var total = fileLength(info.sourceDir)
        info.splitSourceDirs?.forEach { path ->
            total += fileLength(path)
        }
        return total
    }

    private fun fileLength(path: String?): Long {
        if (path.isNullOrEmpty()) return 0L
        return try {
            File(path).length()
        } catch (_: Exception) {
            0L
        }
    }
}
