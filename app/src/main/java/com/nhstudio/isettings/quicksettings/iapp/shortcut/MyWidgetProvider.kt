package com.nhstudio.isettings.quicksettings.iapp.shortcut

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.core.graphics.createBitmap
import com.nhstudio.iapp.appmanager.R


class MyWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, manager, widgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int,
        newOptions: android.os.Bundle
    ) {
        // 🔥 gọi lại khi resize widget
        updateWidget(context, manager, widgetId)



        val intent = Intent(context, MyWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
        }
        context.sendBroadcast(intent)
    }

    private fun updateWidget(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int
    ) {

        val packageName = WidgetHelper.getPackage(context, widgetId)
        val name = WidgetHelper.getName(context, widgetId)
        val showName = WidgetHelper.isShowName(context, widgetId)
        val color = WidgetHelper.getColor(context, widgetId)

        val views = RemoteViews(context.packageName, R.layout.app_shortcut_widget)

        if (packageName != null) {

            val pm = context.packageManager
            val icon = pm.getApplicationIcon(packageName)

            views.setImageViewBitmap(
                R.id.widgetIcon,
                drawableToBitmap(icon)
            )

            val intent = pm.getLaunchIntentForPackage(packageName)

            val pendingIntent = PendingIntent.getActivity(
                context,
                widgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)
        }

        // 🎨 SET COLOR
        views.setTextColor(R.id.widgetName, color)

        // 🔥 AUTO TEXT SIZE
        val options = manager.getAppWidgetOptions(widgetId)
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

        val textSize = calculateTextSize(context,minWidth, minHeight)

        views.setTextViewTextSize(
            R.id.widgetName,
            TypedValue.COMPLEX_UNIT_SP,
            textSize
        )


        // 👻 ẨN / HIỆN TEXT
        if (!showName || name.isNullOrEmpty()) {
            views.setViewVisibility(R.id.widgetName, android.view.View.GONE)
        } else {
            views.setViewVisibility(R.id.widgetName, android.view.View.VISIBLE)
            views.setTextViewText(R.id.widgetName, name)
        }

        manager.updateAppWidget(widgetId, views)
    }

    // 🧠 logic resize

    fun drawableToBitmap(drawable: android.graphics.drawable.Drawable): android.graphics.Bitmap {
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }


    private fun calculateTextSize(context: Context, widthDp: Int, heightDp: Int): Float {

        val density = context.resources.displayMetrics.density
        val sizePx = minOf(widthDp, heightDp) * density

        val baseSize = 300f
        val step = 100f

        val level = ((sizePx - baseSize) / step).toInt().coerceAtLeast(0)

        return 16f + level * 4f
    }
}