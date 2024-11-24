package com.nhstudio.isettings.quicksettings.iapp.extension

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.nhstudio.iapp.appmanager.R
import com.nhstudio.iapp.appmanager.databinding.DialogCreateUserBinding
import com.nhstudio.iapp.appmanager.databinding.ProgressDialogViewBinding
import com.nhstudio.isettings.quicksettings.iapp.data.BaseConfig
import com.nhstudio.isettings.quicksettings.iapp.data.SettingModel
import kotlin.collections.ArrayList

const val PREFS_KEY = "Prefs"
val Context.config: BaseConfig get() = BaseConfig.newInstance(applicationContext)
fun Context.getSharedPrefs() = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

val Fragment.config: BaseConfig? get() = if (context != null) BaseConfig.newInstance(context!!) else null

fun fileExists(context: Context, uri: Uri): Boolean {
    val cursor = context.contentResolver.query(
        uri,
        arrayOf(MediaStore.MediaColumns.DATA),
        null, null, null
    )
    return cursor?.use { it.moveToFirst() } ?: false
}

//fun getListSetting(): ArrayList<SettingModel> {
//    val listSetting = ArrayList<SettingModel>()
//    val sdkAndroid = Build.VERSION.SDK_INT
//    listSetting.add(SettingModel("ACTION_ACCESSIBILITY_SETTINGS",R.drawable.bluetooth, Settings.ACTION_SETTINGS))
//    listSetting.add(SettingModel("ACTION_ADD_ACCOUNT",R.drawable.bluetooth,"android.settings.ADD_ACCOUNT_SETTINGS"))
//    listSetting.add(SettingModel("ACTION_ADD_ACCOUNT",R.drawable.bluetooth,"android.settings.AIRPLANE_MODE_SETTINGS"))
//    return listSetting
//}

fun getListSetting(context: Context): ArrayList<SettingModel> {
    val arrayList: ArrayList<SettingModel> = ArrayList<SettingModel>()
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_ACCESSIBILITY_SETTINGS),
            2131165397,
            "android.settings.ACCESSIBILITY_SETTINGS"

        )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_ADD_ACCOUNT),
            2131165399,
            "android.settings.ADD_ACCOUNT_SETTINGS",

            )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_AIRPLANE_MODE_SETTINGS),
            2131165400,
            "android.settings.AIRPLANE_MODE_SETTINGS",

            )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_APN_SETTINGS),
            2131165443,
            "android.settings.APN_SETTINGS",

            )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_APPLICATION_DETAILS_SETTINGS),
            2131165402,

            "android.settings.APPLICATION_DETAILS_SETTINGS",

            )
    )
    val androidSDk = Build.VERSION.SDK_INT
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
            2131165411,

            "android.settings.APPLICATION_DEVELOPMENT_SETTINGS",

            )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_APPLICATION_SETTINGS),
            2131165402,

            "android.settings.APPLICATION_SETTINGS",

            )
    )
    if (androidSDk >= 29) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_APP_NOTIFICATION_BUBBLE_SETTINGS),
                2131165429,

                "android.settings.APP_NOTIFICATION_BUBBLE_SETTINGS"

            )
        )
    }
    if (androidSDk >= 26) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_APP_NOTIFICATION_SETTINGS),
                2131165429,

                "android.settings.APP_NOTIFICATION_SETTINGS"

            )
        )
    }
    if (androidSDk >= 31) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS),
                2131165444,

                "android.settings.APP_OPEN_BY_DEFAULT_SETTINGS"
            )
        )
    }
    if (androidSDk >= 29) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_APP_SEARCH_SETTINGS),
                2131165436,

                "android.settings.APP_SEARCH_SETTINGS"
            )
        )
    }
    if (androidSDk >= 29) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_APP_USAGE_SETTINGS),
                2131165438,

                "android.settings.action.APP_USAGE_SETTINGS"
            )
        )
    }
    if (androidSDk >= 31) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_AUTO_ROTATE_SETTINGS),
                2131165403,

                "android.settings.AUTO_ROTATE_SETTINGS",

                )
        )
    }
    if (androidSDk >= 22) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_BATTERY_SAVER_SETTINGS),
                2131165415,
                "android.settings.BATTERY_SAVER_SETTINGS"

            )
        )
    }
    if (androidSDk >= 30) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_BIOMETRIC_ENROLL),
                2131165417,

                "android.settings.BIOMETRIC_ENROLL",

                )
        )
    }
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_BLUETOOTH_SETTINGS),
            2131165406,
            "android.settings.BLUETOOTH_SETTINGS",

            )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_CAPTIONING_SETTINGS),
            2131165446,

            "android.settings.CAPTIONING_SETTINGS",

            )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_CAST_SETTINGS),
            2131165409,

            "android.settings.CAST_SETTINGS",

            )
    )
    if (androidSDk >= 30) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_CONDITION_PROVIDER_SETTINGS),
                2131165414,

                "android.settings.ACTION_CONDITION_PROVIDER_SETTINGS",

                )
        )
    }
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_DATA_ROAMING_SETTINGS),
            2131165404,

            "android.settings.DATA_ROAMING_SETTINGS",
        )
    )
    if (androidSDk >= 28) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_DATA_USAGE_SETTINGS),
                2131165404,

                "android.settings.DATA_USAGE_SETTINGS",

                )
        )
    }
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_DATE_SETTINGS),
            2131165410,

            "android.settings.DATE_SETTINGS",

            )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_DEVICE_INFO_SETTINGS),
            2131165412,

            "android.settings.DEVICE_INFO_SETTINGS",

            )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_DISPLAY_SETTINGS),
            2131165430,

            "android.settings.DISPLAY_SETTINGS",

            )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_DREAM_SETTINGS),
            2131165438,

            "android.settings.DREAM_SETTINGS",

            )
    )
    if (androidSDk in 28..29) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_FINGERPRINT_ENROLL),
                2131165417,

                "android.settings.FINGERPRINT_ENROLL",

                )
        )
    }
    if (androidSDk >= 24) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_HARD_KEYBOARD_SETTINGS),
                2131165421,

                "android.settings.HARD_KEYBOARD_SETTINGS"

            )
        )
    }
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_HOME_SETTINGS),
            2131165419,

            "android.settings.HOME_SETTINGS",

            )
    )
    if (androidSDk >= 24) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS),
                2131165404,

                "android.settings.IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS"
            )
        )
    }
    if (androidSDk >= 23) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
                2131165405,

                "android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS",

                )
        )
    }
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_INPUT_METHOD_SETTINGS),
            2131165421,

            "android.settings.INPUT_METHOD_SETTINGS",

            )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS),
            2131165421,

            "android.settings.INPUT_METHOD_SUBTYPE_SETTINGS"

        )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_INTERNAL_STORAGE_SETTINGS),
            2131165445,

            "android.settings.INTERNAL_STORAGE_SETTINGS"

        )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_LOCALE_SETTINGS),
            2131165422,

            "android.settings.LOCALE_SETTINGS",

            )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_LOCATION_SOURCE_SETTINGS),
            2131165424,

            "android.settings.LOCATION_SOURCE_SETTINGS",

            )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS),
            2131165402,

            "android.settings.MANAGE_ALL_APPLICATIONS_SETTINGS",

            )
    )
    if (androidSDk >= 30) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION),
                2131165416,

                "android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION",

                )
        )
    }
    if (androidSDk >= 31) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_MANAGE_ALL_SIM_PROFILES_SETTINGS),
                2131165441,

                "android.settings.MANAGE_ALL_SIM_PROFILES_SETTINGS",

                )
        )
    }
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_MANAGE_APPLICATIONS_SETTINGS),
            2131165402,

            "android.settings.MANAGE_APPLICATIONS_SETTINGS",

            )
    )
    if (androidSDk >= 24) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
                2131165402,

                "android.settings.MANAGE_DEFAULT_APPS_SETTINGS",

                )
        )
    }
    if (androidSDk >= 23) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_MANAGE_OVERLAY_PERMISSION),
                2131165423,

                "android.settings.action.MANAGE_OVERLAY_PERMISSION",

                )
        )
    }
    if (androidSDk >= 26) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_MANAGE_UNKNOWN_APP_SOURCES),
                2131165418,

                "android.settings.MANAGE_UNKNOWN_APP_SOURCES",

                )
        )
    }
    if (androidSDk >= 23) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_MANAGE_WRITE_SETTINGS),
                2131165435,

                "android.settings.action.MANAGE_WRITE_SETTINGS",

                )
        )
    }
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_MEMORY_CARD_SETTINGS),
            2131165425,

            "android.settings.MEMORY_CARD_SETTINGS",

            )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_NETWORK_OPERATOR_SETTINGS),
            2131165408,
            "android.settings.NETWORK_OPERATOR_SETTINGS"

        )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_NFCSHARING_SETTINGS),
            2131165428,

            "android.settings.NFCSHARING_SETTINGS",

            )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_NFC_PAYMENT_SETTINGS),
            2131165428,

            "android.settings.NFC_PAYMENT_SETTINGS",

            )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_NFC_SETTINGS),
            2131165428,

            "android.settings.NFC_SETTINGS",

            )
    )
    if (androidSDk >= 26) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_NIGHT_DISPLAY_SETTINGS),
                2131165427,

                "android.settings.NIGHT_DISPLAY_SETTINGS",

                )
        )
    }
    if (androidSDk >= 29) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_NOTIFICATION_ASSISTANT_SETTINGS),
                2131165429,

                "android.settings.NOTIFICATION_ASSISTANT_SETTINGS",

                )
        )
    }
    if (androidSDk >= 22) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_NOTIFICATION_LISTENER_SETTINGS),
                2131165429,

                "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS",

                )
        )
    }
    if (androidSDk >= 23) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS),
                2131165429,

                "android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS",

                )
        )
    }
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_PRINT_SETTINGS),
            2131165432,

            "android.settings.ACTION_PRINT_SETTINGS",

            )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_PRIVACY_SETTINGS),
            2131165433,

            "android.settings.PRIVACY_SETTINGS",

            )
    )
    if (androidSDk >= 30) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_QUICK_ACCESS_WALLET_SETTINGS),
                2131165452,

                "android.settings.QUICK_ACCESS_WALLET_SETTINGS",

                )
        )
    }
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_QUICK_LAUNCH_SETTINGS),
            2131165439,

            "android.settings.QUICK_LAUNCH_SETTINGS"

        )
    )
    if (androidSDk >= 31) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_REQUEST_MANAGE_MEDIA),
                2131165420,

                "android.settings.REQUEST_MANAGE_MEDIA",

                )
        )
    }
    if (androidSDk >= 31) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_REQUEST_SCHEDULE_EXACT_ALARM),
                2131165401,

                "android.settings.REQUEST_SCHEDULE_EXACT_ALARM",

                )
        )
    }
    if (androidSDk >= 26) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_REQUEST_SET_AUTOFILL_SERVICE),
                2131165448,

                "android.settings.REQUEST_SET_AUTOFILL_SERVICE"

            )
        )
    }
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_SEARCH_SETTINGS),
            2131165436,

            "android.search.action.SEARCH_SETTINGS",

            )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_SECURITY_SETTINGS),
            2131165453,

            "android.settings.SECURITY_SETTINGS",

            )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_SETTINGS),
            2131165431,

            "android.settings.SETTINGS",

            )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_SHOW_REGULATORY_INFO),
            2131165438,

            "android.settings.SHOW_REGULATORY_INFO",

            )
    )
    if (androidSDk >= 30) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_SHOW_WORK_POLICY_INFO),
                2131165407,

                "android.settings.SHOW_WORK_POLICY_INFO"

            )
        )
    }
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_SOUND_SETTINGS),
            2131165442,

            "android.settings.SOUND_SETTINGS"
        )
    )
    if (androidSDk == 28) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_STORAGE_VOLUME_ACCESS_SETTINGS),
                2131165445,

                "android.settings.STORAGE_VOLUME_ACCESS_SETTINGS"
            )
        )
    }
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_SYNC_SETTINGS),
            2131165447,

            "android.settings.SYNC_SETTINGS",
        )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_USAGE_ACCESS_SETTINGS),
            2131165438,

            "android.settings.USAGE_ACCESS_SETTINGS",
        )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_USER_DICTIONARY_SETTINGS),
            2131165413,

            "android.settings.USER_DICTIONARY_SETTINGS"
        )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_VOICE_INPUT_SETTINGS),
            2131165426,

            "android.settings.VOICE_INPUT_SETTINGS"
        )
    )
    if (androidSDk >= 24) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_VPN_SETTINGS),
                2131165450,

                "android.settings.VPN_SETTINGS"
            )
        )
    }
    if (androidSDk >= 24) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_VR_LISTENER_SETTINGS),
                2131165451,

                "android.settings.VR_LISTENER_SETTINGS"
            )
        )
    }
    if (androidSDk >= 24) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_WEBVIEW_SETTINGS),
                2131165455,

                "android.settings.WEBVIEW_SETTINGS"
            )
        )
    }
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_WIFI_IP_SETTINGS),
            2131165454,

            "android.settings.WIFI_IP_SETTINGS",
        )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_WIFI_SETTINGS),
            2131165454,

            "android.settings.WIFI_SETTINGS"
        )
    )
    arrayList.add(
        SettingModel(
            context.getString(R.string.ACTION_WIRELESS_SETTINGS),
            2131165440,

            "android.settings.WIRELESS_SETTINGS",
        )
    )
    if (androidSDk >= 26) {
        arrayList.add(
            SettingModel(
                context.getString(R.string.ACTION_ZEN_MODE_PRIORITY_SETTINGS),
                2131165414,

                "android.settings.ZEN_MODE_PRIORITY_SETTINGS"
            )
        )
    }
//    arrayList.add(
//        SettingModel(
//            ACTION_ADS_SETTINGS),
//            2131165398,
//
//            desc_ACTION_ADS_SETTINGS),
//            ComponentName(
//                "com.google.android.gms",
//                "com.google.android.gms.ads.settings.AdsSettingsActivity"
//            )
//        )
//    )
//    if (i10 >= 29) {
//        arrayList.add(
//            SettingModel(
//                ACTION_MAINLINE_UPDATER),
//                2131165449,
//                desc_ACTION_MAINLINE_UPDATER),
//                ComponentName(
//                    "com.android.vending",
//                    "com.google.android.finsky.systemupdateactivity.SettingsSecurityEntryPoint"
//                )
//            )
//        )
//    }
    return arrayList
}

fun Activity.setFullScreen() {
    checkInter = false
    if (config.setFullScreen) {
        kotlin.runCatching {
            WindowCompat.getInsetsController(window!!, window.decorView).let {
                it?.hide(WindowInsetsCompat.Type.navigationBars())
                it?.hide(WindowInsetsCompat.Type.statusBars())
                it?.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

    } else {
        kotlin.runCatching {
            WindowInsetsControllerCompat(window, window.decorView).apply {
                show(WindowInsetsCompat.Type.statusBars())
                show(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

}

fun Context.showDialogUser(
    activity: Activity,
    onSave: (nameUser: String, detailUser: String) -> Unit,
    onCancel: () -> Unit
) {
    var showToast = false
    val dialog: Dialog
    val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_create_user, null)
    val builder = AlertDialog.Builder(this)
        .setView(view)
        .setCancelable(false)

    val view2 = DialogCreateUserBinding.bind(view)
    dialog = builder.create()
    dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    if (!dialog.isShowing) {
        dialog.show()
    }
    view2.edtPasscode.setText(activity.config.nameUser)
    view2.edtRePasscode.setText(activity.config.detailUser)

    if (darkMode) {
        view2.rootDl.setBackgroundResource(R.drawable.background_dialog_dark)
        view2.tvTitle.setTextColor(Color.WHITE)
        view2.view1Dl.setBackgroundColor(Color.parseColor("#626262"))
        view2.view2Dl.setBackgroundColor(Color.parseColor("#626262"))
        view2.edtPasscode.setBackgroundResource(R.drawable.background_edt_dialog_drak)
        view2.edtRePasscode.setBackgroundResource(R.drawable.background_edt_dialog_drak)
        view2.edtPasscode.setTextColor(Color.WHITE)
        view2.edtRePasscode.setTextColor(Color.WHITE)
    }




    view2.tvCancel.setPreventDoubleClickScaleView(300) {
        view2.edtPasscode.hideKeyboard()
        onCancel()
        dialog.dismiss()
    }

    view2.tvOk.setPreventDoubleClickScaleView(300) {
        view2.edtPasscode.hideKeyboard()
        onSave(view2.edtPasscode.text.toString(),view2.edtRePasscode.text.toString())
        dialog.dismiss()
    }


}

fun showDialogAds(
    context: Context,
    lifecycle: Lifecycle,
) {

    val dialog: Dialog
    val viewBind: View = LayoutInflater.from(context).inflate(R.layout.progress_dialog_view, null)
    val builder = AlertDialog.Builder(context)
        .setView(viewBind)
        .setCancelable(true)

    dialog = builder.create()
    dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    val binding = ProgressDialogViewBinding.bind(viewBind)
    if (darkMode) {
        binding.apply {
            rootAds.setBackgroundColorTin(R.color.bg3)
            tv2.setTextColor(Color.WHITE)
            tvExit.setTextColor(Color.WHITE)
        }

    }
    Handler(Looper.getMainLooper()).postDelayed({
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    },1000)



    lifecycle.addObserver(LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_STOP -> {
                dialog.dismiss()
            }

            else -> {

            }
        }
    })
    if (!dialog.isShowing) {
        dialog.show()
    }


}