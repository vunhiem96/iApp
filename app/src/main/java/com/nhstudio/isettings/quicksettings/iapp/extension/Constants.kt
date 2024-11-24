package com.nhstudio.isettings.quicksettings.iapp.extension

import android.content.pm.ApplicationInfo
import com.nhstudio.iapp.appmanager.BuildConfig



const val PRODUCT_ID_FAKE = "removeadnosale"
       const val PRODUCT_ID = "removead"


var darkMode = false
var checkInter = false
var isTesting = BuildConfig.DEBUG
var loadInterAd = false
var showInterOk = false
var initOpenAds = false
var canShowOpenAds = false


var priceString = "1,29$"
var priceStringFake = "4,2$"
 var defaultSortList: MutableList<ApplicationInfo> = mutableListOf()




