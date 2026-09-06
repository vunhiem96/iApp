package com.nhstudio.isettings.quicksettings.iapp.iap.model

data class InAppModel(
    val name: String = "",
    val productId: String = "",
    val title: String = "",
    val formattedPrice: String = "",
    val priceAmountMicros: Long = 0L,
    val priceCurrencyCode: String = ""
)
