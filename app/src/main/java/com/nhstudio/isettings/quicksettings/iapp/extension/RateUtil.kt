package com.nhstudio.isettings.quicksettings.iapp.extension

import android.content.res.Resources
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import java.util.*

public  class RateUtil {
     fun getDeviceInfo(): String {
        val densityText = when (Resources.getSystem().displayMetrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW -> "LDPI"
            DisplayMetrics.DENSITY_MEDIUM -> "MDPI"
            DisplayMetrics.DENSITY_HIGH -> "HDPI"
            DisplayMetrics.DENSITY_XHIGH -> "XHDPI"
            DisplayMetrics.DENSITY_XXHIGH -> "XXHDPI"
            DisplayMetrics.DENSITY_XXXHIGH -> "XXXHDPI"
            else -> "HDPI"
        }

        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        var megAvailable = 0L
        val bytesAvailable: Long
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
            megAvailable = bytesAvailable / (1024 * 1024)
        }


        return "Device ${Build.MODEL}," +
                " ${Locale.getDefault()}, " +
                "Android ${Build.VERSION.RELEASE}, Screen ${Resources.getSystem().displayMetrics.widthPixels}x${Resources.getSystem().displayMetrics.heightPixels}, " +
                "$densityText, Free space ${megAvailable}MB, TimeZone ${TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT)}"
    }
}