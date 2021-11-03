package io.primer.android.payment.paypal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.R
import io.primer.android.PaymentMethodIntent
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
internal class PayPalDescriptor constructor(
    config: PaymentMethodRemoteConfig,
    private val options: PayPal,
) : PaymentMethodDescriptor(config), DIAppComponent {

    private val localConfig: PrimerConfig by inject()

    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = if (localConfig.paymentMethodIntent == PaymentMethodIntent.VAULT) {
            PayPalBillingAgreementBehaviour(this)
        } else {
            PayPalOrderBehaviour(this)
        }

    override val type: PaymentMethodUiType
        get() = PaymentMethodUiType.SIMPLE_BUTTON

    override val vaultCapability: VaultCapability
        get() = VaultCapability.SINGLE_USE_AND_VAULT

    override fun createButton(container: ViewGroup): View =
        LayoutInflater.from(container.context).inflate(
            R.layout.payment_method_button_paypal,
            container,
            false
        )
}
