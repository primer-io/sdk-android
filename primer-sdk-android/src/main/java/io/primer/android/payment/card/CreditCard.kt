package io.primer.android.payment.card

import android.view.View
import android.view.ViewGroup
import io.primer.android.PAYMENT_CARD_IDENTIFIER
import io.primer.android.PaymentMethod
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.payment.*
import io.primer.android.ui.fragments.CardFormFragment
import io.primer.android.viewmodel.PrimerViewModel
import org.json.JSONObject

internal const val CARD_NAME_FILED_NAME = "cardholderName"
internal const val CARD_NUMBER_FIELD_NAME = "number"
internal const val CARD_EXPIRY_FIELD_NAME = "date"
internal const val CARD_CVV_FIELD_NAME = "cvv"
internal const val CARD_EXPIRY_MONTH_FIELD_NAME = "expirationMonth"
internal const val CARD_EXPIRY_YEAR_FIELD_NAME = "expirationYear"

internal class CreditCard(
  viewModel: PrimerViewModel,
  config: PaymentMethodRemoteConfig,
  private val options: PaymentMethod.Card
  ): PaymentMethodDescriptor(viewModel, config) {

  override val identifier = PAYMENT_CARD_IDENTIFIER

  private val log = Logger("payment-method.$identifier")

  override val selectedBehaviour: SelectedPaymentMethodBehaviour
    get() = NewFragmentBehaviour(CardFormFragment::newInstance)

  override val type: PaymentMethodType
    get() = PaymentMethodType.FORM

  override val vaultCapability: VaultCapability
    get() = VaultCapability.SINGLE_USE_AND_VAULT

  override fun createButton(container: ViewGroup): View {
    View.inflate(container.context, R.layout.payment_method_button_card, container)
    return container.findViewById(R.id.card_preview_button)
  }

  override fun toPaymentInstrument(): JSONObject {
    val json = JSONObject()

    json.put(CARD_NAME_FILED_NAME, values[CARD_NAME_FILED_NAME]?.trim() ?: "")
    json.put(CARD_NUMBER_FIELD_NAME, values[CARD_NUMBER_FIELD_NAME]?.replace("\\s".toRegex(), "") ?: "")
    json.put(CARD_CVV_FIELD_NAME, values[CARD_CVV_FIELD_NAME] ?: "")

    // TODO fix this after masking
    val expiration = values[CARD_EXPIRY_FIELD_NAME] ?: ""

    val month = expiration.substring(0, minOf(expiration.length, 2))
    var year = if (expiration.length > 2) expiration.substring(2) else ""

    if (year.length == 2) {
      year = "20$year"
    }

    json.put(CARD_EXPIRY_MONTH_FIELD_NAME, month)
    json.put(CARD_EXPIRY_YEAR_FIELD_NAME, year)

    return json
  }

  override fun validate(): List<SyncValidationError> {
    val errors = ArrayList<SyncValidationError>()
    val data = toPaymentInstrument()

    // TODO: Better card field validation

    val name = data[CARD_NAME_FILED_NAME] as String

    if (name.isEmpty()) {
      errors.add(SyncValidationError(name = CARD_NAME_FILED_NAME, errorId = R.string.form_error_required, fieldId = R.string.card_holder_name))
    }

    val number = data[CARD_NUMBER_FIELD_NAME] as String

    if (number.isEmpty()) {
      errors.add(SyncValidationError(name = CARD_NUMBER_FIELD_NAME, errorId = R.string.form_error_required, fieldId = R.string.card_number))
    } else if (number.length < 16) {
      errors.add(SyncValidationError(name = CARD_NUMBER_FIELD_NAME, errorId = R.string.form_error_invalid, fieldId = R.string.card_number))
    }

    val cvv = data[CARD_CVV_FIELD_NAME] as String

    if (cvv.isEmpty()) {
      errors.add(SyncValidationError(name = CARD_CVV_FIELD_NAME, errorId = R.string.form_error_required, fieldId = R.string.card_cvv))
    } else if (cvv.length < 3) {
      errors.add(SyncValidationError(name = CARD_CVV_FIELD_NAME, errorId = R.string.form_error_invalid, fieldId = R.string.card_cvv))
    }

    val date = values[CARD_EXPIRY_FIELD_NAME] ?: ""
    val month = data[CARD_EXPIRY_MONTH_FIELD_NAME] as String
    val year = data[CARD_EXPIRY_YEAR_FIELD_NAME] as String

    if (date.trim().isEmpty()) {
      errors.add(SyncValidationError(name = CARD_EXPIRY_FIELD_NAME, errorId = R.string.form_error_required, fieldId = R.string.card_expiry))
    } else if (month.isEmpty() || year.isEmpty()) {
      errors.add(SyncValidationError(name = CARD_EXPIRY_FIELD_NAME, errorId = R.string.form_error_invalid, fieldId = R.string.card_expiry))
    }

    return errors
  }
}