package io.primer.android.payment.async.atome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.R
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor

internal class AtomePaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodRemoteConfig,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    override val title = "ATOME"

    override fun getLoadingResourceId() = R.drawable.ic_logo_atome_square

    override fun createButton(container: ViewGroup): View {
        return LayoutInflater.from(container.context).inflate(
            R.layout.payment_method_button_atome,
            container,
            false
        )
    }
}
