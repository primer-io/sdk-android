package io.primer.android.payment.async.sofort

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodRemoteConfig
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.payment.LoadingState

internal class SofortPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodRemoteConfig,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    override val title = "SOFORT"

    override fun getLoadingState() = LoadingState(R.drawable.ic_logo_sofort_square)

    override fun createButton(container: ViewGroup): View {

        return LayoutInflater.from(container.context).inflate(
            R.layout.payment_method_button_sofort,
            container,
            false
        )
    }
}
