package io.primer.android.payment.async.opennode

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.R
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor

internal class OpenNodePaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodRemoteConfig,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    override val title = "OPENNODE"

    override val behaviours: List<SelectedPaymentMethodBehaviour> =
        listOf(AsyncPaymentMethodBehaviour(this))

    override fun createButton(container: ViewGroup): View {
        return LayoutInflater.from(container.context).inflate(
            R.layout.payment_method_button_opennode,
            container,
            false
        )
    }
}
