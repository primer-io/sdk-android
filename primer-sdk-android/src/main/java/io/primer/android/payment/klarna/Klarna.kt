package io.primer.android.payment.klarna

import android.content.Context
import android.view.View
import io.primer.android.PaymentMethod
import io.primer.android.R
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class Klarna constructor(
    val checkoutConfig: CheckoutConfig,
    val options: PaymentMethod.Klarna,
    config: PaymentMethodRemoteConfig,
) : PaymentMethodDescriptor(config) {

    companion object {

        const val KLARNA_REQUEST_CODE = 1000
    }

    override val identifier: String
        get() = "KLARNA"

    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        // get() = KlarnaBehaviour(this, checkoutConfig.packageName)
        get() = RecurringKlarnaBehaviour(this)

    override val type: PaymentMethodType
        get() = PaymentMethodType.SIMPLE_BUTTON

    override val vaultCapability: VaultCapability
        get() = VaultCapability.SINGLE_USE_AND_VAULT

    override fun createButton(context: Context): View =
        View.inflate(context, R.layout.payment_method_button_klarna, null)
}
