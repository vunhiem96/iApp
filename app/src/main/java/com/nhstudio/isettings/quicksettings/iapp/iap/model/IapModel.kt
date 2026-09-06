package com.nhstudio.isettings.quicksettings.iapp.iap.model

data class IapModel(
    val type: String = "",
    val productId: String = "",
    val subscriptionDetails: List<SubModel> = emptyList(),
    val inAppDetails: InAppModel? = null,
    val isPurchase: Boolean = false,
    val purchaseTime: Long = 0L,
    val purchaseToken: String = "",
    val purchaseState: PurchaseState = PurchaseState.NOT
) {
    fun isSubPackage(): Boolean = type.equals("subs", ignoreCase = true)

    fun isInAppPackage(): Boolean = type.equals("inapp", ignoreCase = true)

    fun getSubSale(): SubModel? = subscriptionDetails.firstOrNull { it.typeSub == TypeSub.Sale }

    fun getSubBase(): SubModel? = subscriptionDetails.firstOrNull { it.typeSub == TypeSub.Base }

    fun getSubTrial(): SubModel? = subscriptionDetails.firstOrNull { it.typeSub == TypeSub.Trail }

    fun isPurchased(): Boolean = purchaseState == PurchaseState.PURCHASED || isPurchase

    fun isPending(): Boolean = purchaseState == PurchaseState.PENDING
}
