package io.primer.android.qrcode

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.core.di.DISdkComponent
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.paymentmethods.HeadlessDefinition
import io.primer.android.paymentmethods.PaymentMethodDescriptor
import io.primer.android.paymentmethods.SDKCapability
import io.primer.android.paymentmethods.VaultCapability
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

internal class QrCodeDescriptor(
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse,
) : PaymentMethodDescriptor(config, localConfig), DISdkComponent {
    override val sdkCapabilities: List<SDKCapability> =
        buildList {
            val supportsHeadless =
                when (config.type) {
                    PaymentMethodType.OMISE_PROMPTPAY.name -> true
                    PaymentMethodType.RAPYD_PROMPTPAY.name -> false
                    PaymentMethodType.XFERS_PAYNOW.name -> false
                    else -> false
                }
            if (supportsHeadless) {
                add(SDKCapability.HEADLESS)
            }
            add(SDKCapability.DROP_IN)
        }
    override val vaultCapability: VaultCapability = VaultCapability.SINGLE_USE_ONLY

    override val headlessDefinition: HeadlessDefinition
        get() =
            HeadlessDefinition(
                listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI),
            )
}
