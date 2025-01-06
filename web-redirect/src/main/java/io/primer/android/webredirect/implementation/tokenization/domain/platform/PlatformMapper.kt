package io.primer.android.webredirect.implementation.tokenization.domain.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.Environment

internal interface PlatformMapper {
    fun getPlatform(paymentMethodType: String): String
}

internal class AdyenVippsMapper(
    private val context: Context,
    private val cacheConfigurationDataSource: CacheConfigurationDataSource,
) :
    PlatformMapper {
    override fun getPlatform(paymentMethodType: String): String {
        val environment = cacheConfigurationDataSource.get().environment
        val intentUri = if (environment == Environment.PRODUCTION) PRODUCTION_ENV else TEST_ENV
        val isAppInstalled = isAppInstalled(context = context, deeplink = intentUri)
        return if (isAppInstalled) PlatformResolver.ANDROID_PLATFORM else PlatformResolver.WEB_PLATFORM
    }

    private fun isAppInstalled(
        context: Context,
        deeplink: String,
    ): Boolean {
        val uri = Uri.parse(deeplink)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val packageManager = context.packageManager
        val componentName = intent.resolveActivity(packageManager)
        return componentName != null
    }

    companion object {
        private const val PRODUCTION_ENV = "vipps://"
        private const val TEST_ENV = "vippsmt://"
    }
}
