package com.nhstudio.isettings.quicksettings.iapp.iap.model

import com.google.gson.annotations.SerializedName

data class IapIdModel(
    @SerializedName("id")
    var idProduct: String = "",
    @SerializedName("type")
    var type: String = "inapp"
)
