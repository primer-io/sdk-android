package io.primer.android.payment

import android.content.Context
import android.view.View
import io.primer.android.PaymentMethod
import io.primer.android.logging.Logger
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.payment.card.CreditCard
import io.primer.android.payment.gocardless.GoCardless
import io.primer.android.payment.paypal.PayPal
import io.primer.android.viewmodel.PrimerViewModel
import org.json.JSONObject
import org.koin.core.component.KoinApiExtension
import java.util.*

@KoinApiExtension
internal abstract class PaymentMethodDescriptor(
    val config: PaymentMethodRemoteConfig,
) {

    abstract val identifier: String

    abstract val selectedBehaviour: SelectedPaymentMethodBehaviour

    abstract val type: PaymentMethodType

    abstract val vaultCapability: VaultCapability

    abstract fun createButton(context: Context): View

    private val values: JSONObject = JSONObject()

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

internal class PaymentMethodDescriptorFactory {

    fun create(
        config: PaymentMethodRemoteConfig,
        options: PaymentMethod,
        viewModel: PrimerViewModel,
    ): PaymentMethodDescriptor? {
        // TODO: hate this - think of a better way
        return when (config.type) {
            PAYMENT_CARD_IDENTIFIER -> CreditCard(config, options as PaymentMethod.Card)
            PAYPAL_IDENTIFIER -> PayPal(viewModel, config, options as PaymentMethod.PayPal)
            GOCARDLESS_IDENTIFIER -> GoCardless(config, options as PaymentMethod.GoCardless)
            else -> null
        }
    }
}
