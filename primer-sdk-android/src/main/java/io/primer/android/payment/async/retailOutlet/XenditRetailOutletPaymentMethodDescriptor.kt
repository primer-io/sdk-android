package io.primer.android.payment.async.retailOutlet

import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.payments.additionalInfo.PrimerCheckoutAdditionalInfoResolver
import io.primer.android.domain.payments.additionalInfo.RetailOutletsCheckoutAdditionalInfoResolver
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SDKCapability
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor

internal class XenditRetailOutletPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodConfigDataResponse,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    override val title: String = config.name.orEmpty()

    override val type: PaymentMethodUiType = PaymentMethodUiType.FORM

    override val sdkCapabilities: List<SDKCapability>
        get() = listOf(SDKCapability.HEADLESS)

    override val additionalInfoResolver: PrimerCheckoutAdditionalInfoResolver
        get() = RetailOutletsCheckoutAdditionalInfoResolver()
}
