package io.primer.android.payment

import android.content.Context
import android.view.View
import io.primer.android.GOOGLE_PAY_IDENTIFIER
import io.primer.android.PAYMENT_CARD_IDENTIFIER
import io.primer.android.PAYPAL_IDENTIFIER
import io.primer.android.PaymentMethod
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.payment.card.CreditCard
import io.primer.android.payment.googlepay.GooglePay
import io.primer.android.payment.paypal.PayPal
import io.primer.android.viewmodel.PrimerViewModel
import org.json.JSONObject
import java.util.*

internal abstract class PaymentMethodDescriptor(
  protected val viewModel: PrimerViewModel,
  val config: PaymentMethodRemoteConfig
) {

  abstract val identifier: String

  abstract val selectedBehaviour: SelectedPaymentMethodBehaviour

  abstract val type: PaymentMethodType

  abstract val vaultCapability: VaultCapability

  abstract fun createButton(context: Context): View

  protected val values: JSONObject = JSONObject()

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

  class Factory(private val viewModel: PrimerViewModel) {
    fun create(
      config: PaymentMethodRemoteConfig,
      options: PaymentMethod
    ): PaymentMethodDescriptor? {
      // TODO: hate this - think of a better way
      return when (config.type) {
        PAYMENT_CARD_IDENTIFIER -> CreditCard(viewModel, config, options as PaymentMethod.Card)
        PAYPAL_IDENTIFIER -> PayPal(viewModel, config, options as PaymentMethod.PayPal)
        GOOGLE_PAY_IDENTIFIER -> GooglePay(viewModel, config, options as PaymentMethod.GooglePay)
        else -> null
      }
    }
  }
}
