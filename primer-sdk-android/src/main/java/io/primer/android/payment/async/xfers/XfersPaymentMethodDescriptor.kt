package io.primer.android.payment.async.xfers

import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.HeadlessDefinition
import io.primer.android.payment.SDKCapability
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.payment.LoadingState

internal class XfersPaymentMethodDescriptor(
    override val options: AsyncPaymentMethod,
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse
) : AsyncPaymentMethodDescriptor(options, localConfig, config) {

    override fun getLoadingState() = LoadingState(R.drawable.ic_logo_xfers_square)

    override val sdkCapabilities: List<SDKCapability>
        get() = listOf(SDKCapability.DROP_IN)

    override val headlessDefinition: HeadlessDefinition?
        get() = null
}
