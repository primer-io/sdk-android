package io.primer.android.webredirect.implementation.tokenization.domain.platform

import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

internal class PlatformResolver : DISdkComponent {
    fun getPlatform(paymentMethodType: String): String {
        return when (paymentMethodType) {
            PaymentMethodType.ADYEN_VIPPS.toString() ->
                AdyenVippsMapper(
                    cacheConfigurationDataSource =
                        resolve(
                            name = ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY,
                        ),
                    context = resolve(),
                ).getPlatform(paymentMethodType)

            else -> ANDROID_PLATFORM
        }
    }

    companion object {
        const val ANDROID_PLATFORM = "ANDROID"
        const val WEB_PLATFORM = "WEB"
    }
}
