package io.primer.android.phoneNumber

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.core.di.DISdkComponent
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.paymentmethods.HeadlessDefinition
import io.primer.android.paymentmethods.PaymentMethodDescriptor
import io.primer.android.paymentmethods.SDKCapability
import io.primer.android.paymentmethods.VaultCapability
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

internal class PhoneNumberDescriptor(
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse,
    private val paymentMethodType: String,
) : PaymentMethodDescriptor(config, localConfig), DISdkComponent {
    override val sdkCapabilities: List<SDKCapability>
        get() =
            listOfNotNull(
                SDKCapability.HEADLESS,
                if (paymentMethodType == PaymentMethodType.ADYEN_MBWAY.name) {
                    SDKCapability.DROP_IN
                } else {
                    null
                },
            )

    override val vaultCapability: VaultCapability = VaultCapability.SINGLE_USE_ONLY

    override val headlessDefinition: HeadlessDefinition
        get() =
            HeadlessDefinition(
                paymentMethodManagerCategories = listOf(PrimerPaymentMethodManagerCategory.RAW_DATA),
                rawDataDefinition = HeadlessDefinition.RawDataDefinition(PrimerPhoneNumberData::class),
            )
}
