package io.primer.android.payment

import android.content.Context
import android.view.View
import io.primer.android.PaymentMethod
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.payment.card.CreditCard
import io.primer.android.payment.gocardless.GoCardless
import io.primer.android.payment.google.GooglePayDescriptor
import io.primer.android.payment.klarna.Klarna
import io.primer.android.payment.paypal.PayPal
import io.primer.android.viewmodel.GooglePayPaymentMethodChecker
import io.primer.android.viewmodel.PaymentMethodChecker
import io.primer.android.viewmodel.PaymentMethodCheckerRegistrar
import org.json.JSONObject
import java.util.Collections

internal abstract class PaymentMethodDescriptor(
    val config: PaymentMethodRemoteConfig,
    private val values: JSONObject = JSONObject(), // FIXME avoid holding JSONObject here
) {

    abstract val identifier: String

    abstract val selectedBehaviour: SelectedPaymentMethodBehaviour

    abstract val type: PaymentMethodType

    abstract val vaultCapability: VaultCapability

    // FIXME this should not be here. a model should not be responsible creating views
    abstract fun createButton(context: Context): View

    // FIXME all this should not be here. a model should not be responsible for parsing itself into json
    protected fun getStringValue(key: String): String {
        return values.optString(key)
    }

    fun setTokenizableValue(key: String, value: String) {
        values.put(key, value)
    }

    fun setTokenizableValue(key: String, value: JSONObject) {
        values.put(key, value)
    }

    open fun validate(): List<SyncValidationError> {
        return Collections.emptyList()
    }

    open fun toPaymentInstrument(): JSONObject {
        return values
    }
}

internal class PaymentMethodDescriptorFactory(
    private val paymentMethodCheckers: PaymentMethodCheckerRegistrar,
) {

    // FIXME this factory should not return null
    // FIXME each payment method should have its own factory and register with this one;
    //  this factory should just delegate to the appropriate one

    fun create(
        checkoutConfig: CheckoutConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
    ): PaymentMethodDescriptor? =
        when (paymentMethodRemoteConfig.type) {
            PAYMENT_CARD_IDENTIFIER -> CreditCard(
                paymentMethodRemoteConfig,
                paymentMethod as PaymentMethod.Card
            )
            PAYPAL_IDENTIFIER -> PayPal(
                paymentMethodRemoteConfig,
                paymentMethod as PaymentMethod.PayPal
            )
            GOCARDLESS_IDENTIFIER -> GoCardless(
                paymentMethodRemoteConfig,
                paymentMethod as PaymentMethod.GoCardless
            )
            KLARNA_IDENTIFIER -> Klarna(
                checkoutConfig,
                paymentMethod as PaymentMethod.Klarna,
                paymentMethodRemoteConfig
            )
            GOOGLE_PAY_IDENTIFIER -> {
                GooglePayDescriptor(
                    checkoutConfig = checkoutConfig,
                    options = paymentMethod as PaymentMethod.GooglePay,
                    paymentMethodChecker = paymentMethodCheckers[GOOGLE_PAY_IDENTIFIER] ?: throw Error(),
                    config = paymentMethodRemoteConfig
                )
            }
            else -> null
        }
}
