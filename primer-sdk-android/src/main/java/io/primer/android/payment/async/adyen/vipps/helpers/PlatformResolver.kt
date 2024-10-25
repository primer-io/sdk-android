package io.primer.android.payment.async.adyen.vipps.helpers

import io.primer.android.components.domain.core.mapper.platform.ANDROID
import io.primer.android.components.domain.core.mapper.platform.AdyenVippsMapper
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.di.DISdkComponent

internal object PlatformResolver : DISdkComponent {

    fun getPlatform(paymentMethodType: String): String {
        return when (paymentMethodType) {
            PaymentMethodType.ADYEN_VIPPS.toString() -> AdyenVippsMapper().getPlatform(paymentMethodType)
            else -> ANDROID
        }
    }
}
