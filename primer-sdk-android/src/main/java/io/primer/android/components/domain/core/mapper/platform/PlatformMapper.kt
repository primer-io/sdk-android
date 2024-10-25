package io.primer.android.components.domain.core.mapper.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.Environment
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.resolve

internal const val ANDROID = "ANDROID"
internal const val WEB = "WEB"

internal interface PlatformMapper : DISdkComponent {
    fun getPlatform(paymentMethodType: String): String
}

internal class AdyenVippsMapper : PlatformMapper {

    override fun getPlatform(paymentMethodType: String): String {
        val environment = resolve<LocalConfigurationDataSource>().getConfiguration().environment
        val intentUri = if (environment == Environment.PRODUCTION) PRODUCTION_ENV else TEST_ENV
        val isAppInstalled = isAppInstalled(intentUri)
        return if (isAppInstalled) ANDROID else WEB
    }

    private fun isAppInstalled(deeplink: String): Boolean {
        val uri = Uri.parse(deeplink)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val packageManager = resolve<Context>().packageManager
        val componentName = intent.resolveActivity(packageManager)
        return componentName != null
    }

    companion object {
        private const val PRODUCTION_ENV = "vipps://"
        private const val TEST_ENV = "vippsmt://"
    }
}
