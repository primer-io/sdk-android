package io.primer.android.payment.async

import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.ui.payment.LoadingState

internal class RapydGrabpayPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodConfigDataResponse,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    override val title = "GRABPAY"

    override fun getLoadingState() = LoadingState(R.drawable.ic_logo_grab_pay_square)
}
