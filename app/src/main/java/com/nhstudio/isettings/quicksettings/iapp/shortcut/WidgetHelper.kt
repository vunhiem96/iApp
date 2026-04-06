package com.nhstudio.isettings.quicksettings.iapp.shortcut

import android.content.Context
import androidx.core.content.edit

object WidgetHelper {

    private const val PREF = "widget_prefs"

    fun saveConfig(
        context: Context,
        widgetId: Int,
        packageName: String,
        appName: String?,
        showName: Boolean,
        textColor: Int,
        iconUri: String?
    ) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        prefs.edit {
            putString("app_$widgetId", packageName)
                .putString("name_$widgetId", appName)
                .putBoolean("show_$widgetId", showName)
                .putInt("color_$widgetId", textColor)
                .putString("icon_$widgetId", iconUri)
        }
    }

    fun getPackage(context: Context, widgetId: Int) =
        context.getSharedPreferences(PREF, 0).getString("app_$widgetId", null)

    fun getName(context: Context, widgetId: Int) =
        context.getSharedPreferences(PREF, 0).getString("name_$widgetId", "")

    fun isShowName(context: Context, widgetId: Int) =
        context.getSharedPreferences(PREF, 0).getBoolean("show_$widgetId", true)


    fun getColor(context: Context, widgetId: Int): Int {
        return context.getSharedPreferences("widget_prefs", 0)
            .getInt("color_$widgetId", 0xFFFFFFFF.toInt()) // default tráº¯ng
    }

    fun getIconUri(context: Context, widgetId: Int): String? {
        return context.getSharedPreferences("widget_prefs", 0)
            .getString("icon_$widgetId", null)
    }
}
