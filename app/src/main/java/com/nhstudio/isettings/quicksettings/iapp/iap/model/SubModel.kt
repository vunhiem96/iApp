package com.nhstudio.isettings.quicksettings.iapp.iap.model

data class SubModel(
    val name: String = "",
    val productId: String = "",
    val title: String = "",
    val formattedPrice: String = "",
    val priceAmountMicros: Long = 0L,
    val priceCurrencyCode: String = "",
    val offerIdToken: String = "",
    var typeSub: TypeSub = TypeSub.Base
)
