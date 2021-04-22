package io.primer.android.payment.google

import android.content.Context
import android.view.View
import io.primer.android.GooglePay
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
import io.primer.android.viewmodel.GooglePayPaymentMethodChecker
import io.primer.android.viewmodel.PaymentMethodChecker
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.core.component.KoinApiExtension

internal class GooglePayDescriptor constructor(
    val checkoutConfig: CheckoutConfig,
    val options: GooglePay,
    val googlePayBridge: GooglePayBridge,
    paymentMethodChecker: PaymentMethodChecker,
    config: PaymentMethodRemoteConfig,
) : PaymentMethodDescriptor(config) {

    companion object {

        const val GOOGLE_PAY_REQUEST_CODE = 1100
    }

    override val identifier: String
        get() = GOOGLE_PAY_IDENTIFIER

    override val selectedBehaviour: SelectedPaymentMethodBehaviour =
        GooglePayBehaviour(
            paymentMethodDescriptor = this,
            googlePayPaymentMethodChecker = paymentMethodChecker,
            googlePayBridge = googlePayBridge
        )

    override val type: PaymentMethodType
        get() = PaymentMethodType.SIMPLE_BUTTON

    override val vaultCapability: VaultCapability
        get() = VaultCapability.SINGLE_USE_AND_VAULT

    val merchantId: String?
        get() = config.options["merchantId"]?.toString()
            ?.replace("\"", "") // FIXME issue with kotlin serialization here

    override fun createButton(context: Context): View =
        View.inflate(context, R.layout.payment_method_button_google, null)
}
