package com.nhstudio.isettings.quicksettings.iapp.cmp

import android.content.Context
import androidx.preference.PreferenceManager

object GDPRUtils {


    var isUserConsent = true
    var isShowCMP = false

    fun isGDPR(applicationContext: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val gdpr = prefs.getInt("IABTCF_gdprApplies", 0)
        return gdpr == 1
    }

    fun isGDPR2(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt("IABTCF_gdprApplies", -1)
    }

    fun canShowAds(applicationContext: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        //https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#in-app-details
        //https://support.google.com/admob/answer/9760862?hl=en&ref_topic=9756841

        val purposeConsent = prefs.getString("IABTCF_PurposeConsents", "") ?: ""
        val vendorConsent = prefs.getString("IABTCF_VendorConsents", "") ?: ""
        val vendorLI = prefs.getString("IABTCF_VendorLegitimateInterests", "") ?: ""
        val purposeLI = prefs.getString("IABTCF_PurposeLegitimateInterests", "") ?: ""

        val googleId = 755
        val hasGoogleVendorConsent = hasAttribute(vendorConsent, index = googleId)
        val hasGoogleVendorLI = hasAttribute(vendorLI, index = googleId)

        // Minimum required for at least non-personalized ads
        return hasConsentFor(
            listOf(1), purposeConsent, hasGoogleVendorConsent
        ) && hasConsentOrLegitimateInterestFor(
            listOf(2, 7, 9, 10),
            purposeConsent,
            purposeLI,
            hasGoogleVendorConsent,
            hasGoogleVendorLI
        )

    }

//    fun canShowPersonalizedAds(): Boolean {
//        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
//
//        //https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#in-app-details
//        //https://support.google.com/admob/answer/9760862?hl=en&ref_topic=9756841
//
//        val purposeConsent = prefs.getString("IABTCF_PurposeConsents", "") ?: ""
//        val vendorConsent = prefs.getString("IABTCF_VendorConsents","") ?: ""
//        val vendorLI = prefs.getString("IABTCF_VendorLegitimateInterests","") ?: ""
//        val purposeLI = prefs.getString("IABTCF_PurposeLegitimateInterests","") ?: ""
//
//        val googleId = 755
//        val hasGoogleVendorConsent = hasAttribute(vendorConsent, index=googleId)
//        val hasGoogleVendorLI = hasAttribute(vendorLI, index=googleId)
//
//        return hasConsentFor(listOf(1,3,4), purposeConsent, hasGoogleVendorConsent)
//                && hasConsentOrLegitimateInterestFor(listOf(2,7,9,10), purposeConsent, purposeLI, hasGoogleVendorConsent, hasGoogleVendorLI)
//    }

    // Check if a binary string has a "1" at position "index" (1-based)
    private fun hasAttribute(input: String, index: Int): Boolean {
        return input.length >= index && input[index - 1] == '1'
    }

    // Check if consent is given for a list of purposes
    private fun hasConsentFor(
        purposes: List<Int>, purposeConsent: String, hasVendorConsent: Boolean
    ): Boolean {
        return purposes.all { p -> hasAttribute(purposeConsent, p) } && hasVendorConsent
    }

    // Check if a vendor either has consent or legitimate interest for a list of purposes
    private fun hasConsentOrLegitimateInterestFor(
        purposes: List<Int>,
        purposeConsent: String,
        purposeLI: String,
        hasVendorConsent: Boolean,
        hasVendorLI: Boolean
    ): Boolean {
        return purposes.all { p ->
            (hasAttribute(purposeLI, p) && hasVendorLI) || (hasAttribute(
                purposeConsent, p
            ) && hasVendorConsent)
        }
    }
}