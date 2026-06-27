package com.nhstudio.isettings.quicksettings.iapp.cmp

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

class CMPControllerNew constructor(
    private val activity: Activity,
    private val timeoutMs: Long = DEFAULT_TIMEOUT_MS
) {

    fun showCMP(isTesting: Boolean, cmpCallback: CMPCallback) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        val callbackFired = AtomicBoolean(false)
        val handler = Handler(Looper.getMainLooper())

        fun fireShowAd() {
            GDPRUtils.isUserConsent = true
            if (callbackFired.compareAndSet(false, true)) {
                cmpCallback.onShowAd()
            }
        }

        fun fireChangeScreen() {
            GDPRUtils.isUserConsent = false
            if (callbackFired.compareAndSet(false, true)) {
                cmpCallback.onChangeScreen()
            }
        }

        // Chuẩn Google: load ad ngay nếu đã có consent, nhưng vẫn refresh bên dưới
        if (consentInformation.canRequestAds()) {
            fireShowAd()
            // Không return — vẫn gọi requestConsentInfoUpdate để refresh consent cho lần sau
        }

        // Timeout cho requestConsentInfoUpdate: UMP không phản hồi sau 5s → load ad luôn
        val timeoutRunnable = Runnable { fireShowAd() }
        handler.postDelayed(timeoutRunnable, timeoutMs)

        consentInformation.requestConsentInfoUpdate(activity, buildParams(isTesting), {
            handler.removeCallbacks(timeoutRunnable)

            // Chuẩn Google: loadAndShowConsentFormIfRequired tự quyết định có cần show form không
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                if (formError != null) {
                    // Form lỗi (network, v.v.) → fallback load ad luôn
                    fireShowAd()
                    return@loadAndShowConsentFormIfRequired
                }
                // Sau khi form đóng hoặc không cần form → check lại
                if (consentInformation.canRequestAds()) {
                    fireShowAd()
                } else {
                    // GDPR user từ chối consent
                    fireChangeScreen()
                }
            }
        }, { _ ->
            // requestConsentInfoUpdate network error → load ad luôn
            handler.removeCallbacks(timeoutRunnable)
            fireShowAd()
        })
    }

    private fun buildParams(isTesting: Boolean): ConsentRequestParameters {
        val builder = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)

        if (isTesting) {
            val debugSettings = ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId(Utils.getDeviceID(activity))
                .build()
            builder.setConsentDebugSettings(debugSettings)
        }

        return builder.build()
    }

    companion object {
        const val DEFAULT_TIMEOUT_MS = 7000L
    }
}
