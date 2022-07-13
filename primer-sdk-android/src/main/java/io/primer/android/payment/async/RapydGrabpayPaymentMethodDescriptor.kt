package io.primer.android.payment.async

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodRemoteConfig
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.ui.payment.LoadingState

internal class RapydGrabpayPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodRemoteConfig,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    override val title = "GRABPAY"

    override fun getLoadingState() = LoadingState(R.drawable.ic_logo_grab_pay_square)

    override fun createButton(container: ViewGroup): View {
        return LayoutInflater.from(container.context).inflate(
            R.layout.payment_method_button_grabpay,
            container,
            false
        )
    }
}
