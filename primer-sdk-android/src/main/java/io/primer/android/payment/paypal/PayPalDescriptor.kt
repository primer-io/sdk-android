package io.primer.android.payment.paypal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.payment.PAYPAL_IDENTIFIER
import io.primer.android.R
import io.primer.android.UXMode
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
internal class PayPalDescriptor constructor(
    config: PaymentMethodRemoteConfig,
    private val options: PayPal,
) : PaymentMethodDescriptor(config), DIAppComponent {

    private val checkoutConfig: CheckoutConfig by inject()

    override val identifier: String
        get() = PAYPAL_IDENTIFIER

    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = if (checkoutConfig.uxMode == UXMode.VAULT) {
            PayPalBillingAgreementBehaviour(this)
        } else {
            PayPalOrderBehaviour(this)
        }

    override val type: PaymentMethodType
        get() = PaymentMethodType.SIMPLE_BUTTON

    override val vaultCapability: VaultCapability
        get() = VaultCapability.SINGLE_USE_AND_VAULT

    override fun createButton(container: ViewGroup): View =
        LayoutInflater.from(container.context).inflate(
            R.layout.payment_method_button_paypal,
            container,
            false
        )
}
