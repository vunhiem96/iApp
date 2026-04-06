# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.example.iaplibrary.model.IapModel {
    <fields>;
}
-keep class com.example.iaplibrary.model.IapIdModel {
    <fields>;
}


-keep class com.hjq.permissions.** {*;}

-keep class com.google.android.gms.internal.ads.** { *; }

-keep class com.google.android.gms.internal.consent_sdk.** { <fields>; }

-dontwarn android.media.LoudnessCodecController$OnLoudnessCodecUpdateListener
-dontwarn android.media.LoudnessCodecController

-dontwarn com.android.billingclient.api.AcknowledgePurchaseParams$Builder
-dontwarn com.android.billingclient.api.AcknowledgePurchaseParams
-dontwarn com.android.billingclient.api.AcknowledgePurchaseResponseListener
-dontwarn com.android.billingclient.api.BillingClient$Builder
-dontwarn com.android.billingclient.api.BillingClient
-dontwarn com.android.billingclient.api.BillingClientKotlinKt
-dontwarn com.android.billingclient.api.BillingClientStateListener
-dontwarn com.android.billingclient.api.BillingFlowParams$Builder
-dontwarn com.android.billingclient.api.BillingFlowParams$ProductDetailsParams$Builder
-dontwarn com.android.billingclient.api.BillingFlowParams$ProductDetailsParams
-dontwarn com.android.billingclient.api.BillingFlowParams
-dontwarn com.android.billingclient.api.BillingResult$Builder
-dontwarn com.android.billingclient.api.BillingResult
-dontwarn com.android.billingclient.api.ConsumeParams$Builder
-dontwarn com.android.billingclient.api.ConsumeParams
-dontwarn com.android.billingclient.api.ConsumeResult
-dontwarn com.android.billingclient.api.ProductDetails$OneTimePurchaseOfferDetails
-dontwarn com.android.billingclient.api.ProductDetails$PricingPhase
-dontwarn com.android.billingclient.api.ProductDetails$PricingPhases
-dontwarn com.android.billingclient.api.ProductDetails$SubscriptionOfferDetails
-dontwarn com.android.billingclient.api.ProductDetails
-dontwarn com.android.billingclient.api.ProductDetailsResult
-dontwarn com.android.billingclient.api.Purchase
-dontwarn com.android.billingclient.api.PurchasesResult
-dontwarn com.android.billingclient.api.PurchasesUpdatedListener
-dontwarn com.android.billingclient.api.QueryProductDetailsParams$Builder
-dontwarn com.android.billingclient.api.QueryProductDetailsParams$Product$Builder
-dontwarn com.android.billingclient.api.QueryProductDetailsParams$Product
-dontwarn com.android.billingclient.api.QueryProductDetailsParams
-dontwarn com.android.billingclient.api.QueryPurchasesParams$Builder
-dontwarn com.android.billingclient.api.QueryPurchasesParams
-dontwarn com.google.gson.Gson
-dontwarn com.google.gson.annotations.SerializedName
