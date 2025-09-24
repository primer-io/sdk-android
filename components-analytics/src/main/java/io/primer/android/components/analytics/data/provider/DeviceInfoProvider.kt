package io.primer.android.components.analytics.data.provider

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

internal class DeviceInfoProvider(
    private val context: Context,
) {
    fun getDevice(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    fun getDeviceType(): String {
        return if (isTablet()) "tablet" else "phone"
    }

    fun getUserLocale(): String {
        return Locale.getDefault().toLanguageTag()
    }

    private fun isTablet(): Boolean {
        val configuration = context.resources.configuration
        val screenLayout = configuration.screenLayout
        return (screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }
}
