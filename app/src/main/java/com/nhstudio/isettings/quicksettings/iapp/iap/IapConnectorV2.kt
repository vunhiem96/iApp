package com.nhstudio.isettings.quicksettings.iapp.iap

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.nhstudio.isettings.quicksettings.iapp.iap.model.IapIdModel
import com.nhstudio.isettings.quicksettings.iapp.iap.model.IapModel
import com.nhstudio.isettings.quicksettings.iapp.iap.model.InAppModel
import com.nhstudio.isettings.quicksettings.iapp.iap.model.PurchaseState
import com.nhstudio.isettings.quicksettings.iapp.iap.model.SubModel
import com.nhstudio.isettings.quicksettings.iapp.iap.model.TypeSub
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

/**
 * In-app Billing 9 replacement for LibIAP-2.0.3.aar.
 * Keeps the same public API used by the app.
 */
object IapConnectorV2 : PurchasesUpdatedListener {

    const val INAPP = BillingClient.ProductType.INAPP
    const val SUBS = BillingClient.ProductType.SUBS

    private const val TAG = "IapConnectorV2"

    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var application: Application? = null
    private var assetConfig: String = "iap_id.json"
    private var timeoutMs: Long = 10_000L
    private var isDebug: Boolean = false
    private var initialized = false

    private var billingClient: BillingClient? = null
    private val productIds = CopyOnWriteArrayList<IapIdModel>()
    private val listeners = CopyOnWriteArrayList<SubscribeInterface>()

    private val _listAllProductDetailsState =
        MutableStateFlow<List<ProductDetails>>(emptyList())
    val listAllProductDetailsState: StateFlow<List<ProductDetails>> =
        _listAllProductDetailsState.asStateFlow()

    private val _listAllPurchasedState =
        MutableStateFlow<List<Purchase>>(emptyList())
    val listAllPurchasedState: StateFlow<List<Purchase>> =
        _listAllPurchasedState.asStateFlow()

    private val _listAllProductModelState =
        MutableStateFlow<List<IapModel>>(emptyList())
    val listAllProductModelState: StateFlow<List<IapModel>> =
        _listAllProductModelState.asStateFlow()

    val listPurchased = MutableLiveData<List<String>>(emptyList())

    fun getIsDebug(): Boolean = isDebug
    fun getTimeout(): Long = timeoutMs
    fun getAssetConfig(): String = assetConfig
    fun getBilling(): BillingClient? = billingClient
    fun checkInit(): Boolean = initialized

    fun initIap(
        app: Application,
        assetJson: String = "iap_id.json",
        timeout: Long = 10_000L,
        debug: Boolean = false
    ) {
        application = app
        assetConfig = assetJson
        timeoutMs = timeout
        isDebug = debug
        productIds.clear()
        productIds.addAll(loadJsonConfig(app, assetJson))
        initialized = true
        initBilling(app)
    }

    fun addIAPListener(listener: SubscribeInterface) {
        if (!listeners.contains(listener)) listeners.add(listener)
    }

    fun removeIAPListener(listener: SubscribeInterface) {
        listeners.remove(listener)
    }

    fun clearAllIAPListener() {
        listeners.clear()
    }

    fun getAllProductModel(): List<IapModel> = _listAllProductModelState.value

    fun getProductDetailByProductId(productId: String): ProductDetails? =
        _listAllProductDetailsState.value.firstOrNull { it.productId == productId }

    fun inAppInformation(productId: String): InAppModel? =
        getAllProductModel().firstOrNull { it.productId == productId }?.inAppDetails

    fun typeIap(productId: String): String =
        productIds.firstOrNull { it.idProduct == productId }?.type ?: INAPP

    fun buyIap(activity: Activity, productId: String, offerToken: String? = null) {
        val client = billingClient
        if (client == null || !client.isReady) {
            notifyError(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED, "Billing not ready")
            startConnectionIfNeeded()
            return
        }

        val details = getProductDetailByProductId(productId)
        if (details == null) {
            notifyError(BillingClient.BillingResponseCode.ITEM_UNAVAILABLE, "Product not found: $productId")
            refreshData()
            return
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .apply {
                if (details.productType == SUBS) {
                    val token = offerToken
                        ?: details.subscriptionOfferDetails?.firstOrNull()?.offerToken
                    if (!token.isNullOrBlank()) {
                        setOfferToken(token)
                    }
                }
            }
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        client.launchBillingFlow(activity, flowParams)
    }

    fun buyIap(activity: Activity, productId: String, typeSub: TypeSub) {
        val token = getAllProductModel()
            .firstOrNull { it.productId == productId }
            ?.subscriptionDetails
            ?.firstOrNull { it.typeSub == typeSub }
            ?.offerIdToken
        buyIap(activity, productId, token)
    }

    fun resetIap(activity: Activity) {
        if (!isDebug) return
        val client = billingClient ?: return
        scope.launch {
            val purchases = queryPurchases(INAPP) + queryPurchases(SUBS)
            purchases.forEach { purchase ->
                val params = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                client.consumeAsync(params) { result, _ ->
                    log("resetIap consume ${purchase.products}: ${result.responseCode}")
                }
            }
            refreshData()
        }
    }

    fun consumeInAppByProductIap(
        productId: String,
        onSuccess: () -> Unit,
        onError: (String, Int) -> Unit
    ) {
        val client = billingClient
        if (client == null || !client.isReady) {
            onError("Billing not ready", BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)
            return
        }
        scope.launch {
            val purchase = queryPurchases(INAPP).firstOrNull { it.products.contains(productId) }
            if (purchase == null) {
                mainHandler.post {
                    onError("Purchase not found", BillingClient.BillingResponseCode.ITEM_NOT_OWNED)
                }
                return@launch
            }
            val params = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            client.consumeAsync(params) { result, _ ->
                mainHandler.post {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        onSuccess()
                        refreshData()
                    } else {
                        onError(result.debugMessage.orEmpty(), result.responseCode)
                    }
                }
            }
        }
    }

    fun checkPurchasedOnce(timeout: Long = timeoutMs, callback: (Boolean) -> Unit) {
        scope.launch {
            ensureConnected()
            val purchased = queryPurchases(INAPP) + queryPurchases(SUBS)
            mainHandler.post { callback(purchased.isNotEmpty()) }
        }
    }

    fun checkIDPurchasedOnce(timeout: Long = timeoutMs, callback: (List<String>) -> Unit) {
        scope.launch {
            ensureConnected()
            val ids = (queryPurchases(INAPP) + queryPurchases(SUBS))
                .flatMap { it.products }
                .distinct()
            mainHandler.post { callback(ids) }
        }
    }

    fun getErrorMessage(code: Int): String = when (code) {
        BillingClient.BillingResponseCode.OK -> "OK"
        BillingClient.BillingResponseCode.USER_CANCELED -> "User canceled"
        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "Service unavailable"
        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "Billing unavailable"
        BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "Item unavailable"
        BillingClient.BillingResponseCode.DEVELOPER_ERROR -> "Developer error"
        BillingClient.BillingResponseCode.ERROR -> "Error"
        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> "Item already owned"
        BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> "Item not owned"
        BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> "Service disconnected"
        else -> "Unknown ($code)"
    }

    fun getCurrentStateBilling(): String =
        if (billingClient?.isReady == true) "CONNECTED" else "DISCONNECTED"

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases.isNullOrEmpty()) return
                scope.launch {
                    purchases.forEach { handlePurchase(it) }
                    refreshData()
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                notifyError(billingResult.responseCode, "User canceled")
            }
            else -> notifyError(billingResult.responseCode, billingResult.debugMessage.orEmpty())
        }
    }

    private fun initBilling(app: Application) {
        billingClient?.endConnection()
        billingClient = BillingClient.newBuilder(app)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .enableAutoServiceReconnection()
            .build()
        startConnectionIfNeeded()
    }

    private fun startConnectionIfNeeded() {
        val client = billingClient ?: return
        if (client.isReady) {
            refreshData()
            return
        }
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    refreshData()
                } else {
                    log("Billing setup failed: ${billingResult.responseCode} ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                log("Billing service disconnected")
            }
        })
    }

    private fun refreshData() {
        scope.launch {
            ensureConnected()
            val details = queryAllProductDetails()
            val purchases = queryPurchases(INAPP) + queryPurchases(SUBS)
            val models = combine(details, purchases)
            val purchasedIds = purchases.flatMap { it.products }.distinct()

            _listAllProductDetailsState.value = details
            _listAllPurchasedState.value = purchases
            _listAllProductModelState.value = models
            mainHandler.post { listPurchased.value = purchasedIds }
        }
    }

    private suspend fun ensureConnected() {
        val client = billingClient ?: return
        if (client.isReady) return
        startConnectionIfNeeded()
    }

    private fun queryAllProductDetails(): List<ProductDetails> {
        val client = billingClient ?: return emptyList()
        if (!client.isReady) return emptyList()

        val inAppIds = productIds.filter { it.type.equals(INAPP, true) }.map { it.idProduct }
        val subsIds = productIds.filter { it.type.equals(SUBS, true) }.map { it.idProduct }
        val result = mutableListOf<ProductDetails>()
        if (inAppIds.isNotEmpty()) {
            result += queryProductDetailsSync(client, inAppIds, INAPP)
        }
        if (subsIds.isNotEmpty()) {
            result += queryProductDetailsSync(client, subsIds, SUBS)
        }
        return result
    }

    private fun queryProductDetailsSync(
        client: BillingClient,
        ids: List<String>,
        type: String
    ): List<ProductDetails> {
        val products = ids.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(type)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(products)
            .build()

        val latch = java.util.concurrent.CountDownLatch(1)
        var details: List<ProductDetails> = emptyList()
        client.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                details = queryProductDetailsResult.productDetailsList
            } else {
                log("queryProductDetails failed ($type): ${billingResult.responseCode}")
            }
            latch.countDown()
        }
        latch.await(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
        return details
    }

    private fun queryPurchases(type: String): List<Purchase> {
        val client = billingClient ?: return emptyList()
        if (!client.isReady) return emptyList()

        val latch = java.util.concurrent.CountDownLatch(1)
        var purchases: List<Purchase> = emptyList()
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(type)
            .build()
        client.queryPurchasesAsync(params) { billingResult, list ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases = list.orEmpty()
            }
            latch.countDown()
        }
        latch.await(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
        return purchases
    }

    private fun combine(
        details: List<ProductDetails>,
        purchases: List<Purchase>
    ): List<IapModel> {
        val purchaseByProduct = HashMap<String, Purchase>()
        purchases.forEach { purchase ->
            purchase.products.forEach { id -> purchaseByProduct[id] = purchase }
        }

        return details.map { detail ->
            val purchase = purchaseByProduct[detail.productId]
            val state = when (purchase?.purchaseState) {
                Purchase.PurchaseState.PURCHASED -> PurchaseState.PURCHASED
                Purchase.PurchaseState.PENDING -> PurchaseState.PENDING
                else -> PurchaseState.NOT
            }
            IapModel(
                type = detail.productType,
                productId = detail.productId,
                subscriptionDetails = mapSubs(detail),
                inAppDetails = mapInApp(detail),
                isPurchase = state == PurchaseState.PURCHASED,
                purchaseTime = purchase?.purchaseTime ?: 0L,
                purchaseToken = purchase?.purchaseToken.orEmpty(),
                purchaseState = state
            )
        }
    }

    private fun mapInApp(detail: ProductDetails): InAppModel? {
        if (detail.productType != INAPP) return null
        val offer = detail.oneTimePurchaseOfferDetails ?: return InAppModel(
            name = detail.name,
            productId = detail.productId,
            title = detail.title
        )
        return InAppModel(
            name = detail.name,
            productId = detail.productId,
            title = detail.title,
            formattedPrice = offer.formattedPrice,
            priceAmountMicros = offer.priceAmountMicros,
            priceCurrencyCode = offer.priceCurrencyCode
        )
    }

    private fun mapSubs(detail: ProductDetails): List<SubModel> {
        if (detail.productType != SUBS) return emptyList()
        return detail.subscriptionOfferDetails.orEmpty().mapIndexed { index, offer ->
            val phase = offer.pricingPhases.pricingPhaseList.firstOrNull()
            val type = when {
                offer.offerId != null && phase?.priceAmountMicros == 0L -> TypeSub.Trail
                index == 0 && offer.offerId != null -> TypeSub.Sale
                else -> TypeSub.Base
            }
            SubModel(
                name = detail.name,
                productId = detail.productId,
                title = detail.title,
                formattedPrice = phase?.formattedPrice.orEmpty(),
                priceAmountMicros = phase?.priceAmountMicros ?: 0L,
                priceCurrencyCode = phase?.priceCurrencyCode.orEmpty(),
                offerIdToken = offer.offerToken,
                typeSub = type
            )
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        val client = billingClient ?: return
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            val latch = java.util.concurrent.CountDownLatch(1)
            var ackOk = false
            client.acknowledgePurchase(params) { result ->
                ackOk = result.responseCode == BillingClient.BillingResponseCode.OK
                latch.countDown()
            }
            latch.await(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            if (!ackOk) {
                notifyError(BillingClient.BillingResponseCode.ERROR, "Acknowledge failed")
                return
            }
        }

        val model = getAllProductModel().firstOrNull { purchase.products.contains(it.productId) }
            ?: IapModel(
                type = typeIap(purchase.products.firstOrNull().orEmpty()),
                productId = purchase.products.firstOrNull().orEmpty(),
                isPurchase = true,
                purchaseTime = purchase.purchaseTime,
                purchaseToken = purchase.purchaseToken,
                purchaseState = PurchaseState.PURCHASED
            )
        notifySuccess(model.copy(isPurchase = true, purchaseState = PurchaseState.PURCHASED))
    }

    private fun loadJsonConfig(context: Context, assetName: String): List<IapIdModel> {
        return try {
            val json = context.assets.open(assetName).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<IapIdModel>>() {}.type
            Gson().fromJson<List<IapIdModel>>(json, type).orEmpty()
        } catch (e: Exception) {
            log("loadJsonConfig error: ${e.message}")
            emptyList()
        }
    }

    private fun notifySuccess(model: IapModel) {
        mainHandler.post {
            listeners.forEach { it.subscribeSuccess(model) }
        }
    }

    private fun notifyError(code: Int, message: String) {
        mainHandler.post {
            listeners.forEach { it.subscribeError(code, message) }
        }
    }

    private fun log(message: String) {
        if (isDebug) Log.d(TAG, message)
    }
}
