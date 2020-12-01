package io.primer.android.payment.card

import android.view.View
import android.view.ViewGroup
import io.primer.android.PAYMENT_CARD_IDENTIFIER
import io.primer.android.PaymentMethod
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodRemoteConfig
import io.primer.android.payment.PaymentMethodType
import io.primer.android.payment.VaultCapability
import io.primer.android.viewmodel.PrimerViewModel
import org.json.JSONObject
import kotlin.collections.HashMap

internal class CreditCard(
  viewModel: PrimerViewModel,
  config: PaymentMethodRemoteConfig,
  private val options: PaymentMethod.Card
  ): PaymentMethodDescriptor(viewModel, config) {

  override val identifier = PAYMENT_CARD_IDENTIFIER

  private val log = Logger("payment-method.$identifier")

  override val type: PaymentMethodType
    get() = PaymentMethodType.FORM

  override val vaultCapability: VaultCapability
    get() = VaultCapability.SINGLE_USE_AND_VAULT

  private var values: MutableMap<String, String> = HashMap();

  override fun toPaymentInstrument(): JSONObject {
    return JSONObject(values.toMap())
  }

  override fun createButton(container: ViewGroup): View {
    View.inflate(container.context, R.layout.pm_credit_card_button_layout, container)

    val button = container.findViewById<View>(R.id.card_preview_button)

    return button
  }
}