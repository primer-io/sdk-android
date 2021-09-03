package io.primer.android.payment.apaya

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.R
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.payment.APAYA_IDENTIFIER
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class ApayaDescriptor constructor(
    val checkoutConfig: CheckoutConfig,
    val options: Apaya,
    config: PaymentMethodRemoteConfig,
) : PaymentMethodDescriptor(config) {

    companion object {

        const val APAYA_REQUEST_CODE = 1001
    }

    override val identifier: String = APAYA_IDENTIFIER

    override val selectedBehaviour: SelectedPaymentMethodBehaviour = RecurringApayaBehaviour(this)

    override val type: PaymentMethodType
        get() = PaymentMethodType.SIMPLE_BUTTON

    override val vaultCapability: VaultCapability
        get() = VaultCapability.VAULT_ONLY

    override fun createButton(container: ViewGroup): View =
        LayoutInflater.from(container.context).inflate(
            R.layout.payment_method_button_pay_mobile,
            container,
            false
        )
}
