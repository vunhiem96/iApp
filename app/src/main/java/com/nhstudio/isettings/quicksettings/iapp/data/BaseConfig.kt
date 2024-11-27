package com.nhstudio.isettings.quicksettings.iapp.data

import android.content.Context
import com.nhstudio.isettings.quicksettings.iapp.extension.getSharedPrefs

open class BaseConfig(val context: Context) {
    protected val prefs = context.getSharedPrefs()

    companion object {
        fun newInstance(context: Context) = BaseConfig(context)
    }


    var pu: Boolean
        get() = prefs.getBoolean("pu", true)
        set(pu) = prefs.edit().putBoolean("pu", pu).apply()

    var darkMode: Int
        get() = prefs.getInt("darkMode", 1)
        set(darkMode) = prefs.edit().putInt("darkMode", darkMode).apply()

    var rateApp: Boolean?
        get() = prefs.getBoolean("rateApp", false)
        set(rateApp) = prefs.edit().putBoolean("rateApp", rateApp!!).apply()

    var setFullScreen: Boolean
        get() = prefs.getBoolean("setFullScreen", false)
        set(setFullScreen) = prefs.edit()
            .putBoolean("setFullScreen", setFullScreen).apply()

    var nameUser: String
        get() = prefs.getString("nameUser", "")!!
        set(nameUser) = prefs.edit().putString("nameUser", nameUser).apply()

    var avatarUser: String
        get() = prefs.getString("avatarUser", "")!!
        set(avatarUser) = prefs.edit().putString("avatarUser", avatarUser).apply()

    var detailUser: String
        get() = prefs.getString("detailUser", "")!!
        set(detailUser) = prefs.edit().putString("detailUser", detailUser).apply()


    var loadAd: Boolean
        get() = prefs.getBoolean("loadAd", false)
        set(loadAd) = prefs.edit()
            .putBoolean("loadAd", loadAd).apply()

    var showToast: Boolean
        get() = prefs.getBoolean("showToast", true)
        set(showToast) = prefs.edit()
            .putBoolean("showToast", showToast).apply()

}