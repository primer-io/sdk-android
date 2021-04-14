package io.primer.android.payment.google

import android.content.Context
import android.view.View
import io.primer.android.PaymentMethod
import io.primer.android.R
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.payment.GOOGLE_PAY_IDENTIFIER
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import io.primer.android.payment.klarna.KlarnaBehaviour
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class GooglePay constructor(
    val checkoutConfig: CheckoutConfig,
    val options: PaymentMethod.GooglePay,
    config: PaymentMethodRemoteConfig,
) : PaymentMethodDescriptor(config) {

    companion object {
        // const val KLARNA_REQUEST_CODE = 1000
    }

    override val identifier: String
        get() = GOOGLE_PAY_IDENTIFIER

    override val selectedBehaviour: SelectedPaymentMethodBehaviour =
        GooglePayBehaviour(this)

    override val type: PaymentMethodType
        get() = PaymentMethodType.SIMPLE_BUTTON

    override val vaultCapability: VaultCapability
        get() = VaultCapability.SINGLE_USE_AND_VAULT

    override fun createButton(context: Context): View =
        View.inflate(context, R.layout.payment_method_button_klarna, null)
}
