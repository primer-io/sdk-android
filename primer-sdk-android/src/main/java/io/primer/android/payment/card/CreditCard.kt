package io.primer.android.payment.card

import android.content.Context
import android.view.View
import android.widget.TextView
import io.primer.android.payment.PAYMENT_CARD_IDENTIFIER
import io.primer.android.PaymentMethod
import io.primer.android.R
import io.primer.android.UXMode
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.payment.*
import io.primer.android.ui.CardNumberFormatter
import io.primer.android.ui.ExpiryDateFormatter
import io.primer.android.ui.fragments.CardFormFragment
import org.json.JSONObject
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

internal const val CARD_NAME_FILED_NAME = "cardholderName"
internal const val CARD_NUMBER_FIELD_NAME = "number"
internal const val CARD_EXPIRY_FIELD_NAME = "date"
internal const val CARD_CVV_FIELD_NAME = "cvv"
internal const val CARD_EXPIRY_MONTH_FIELD_NAME = "expirationMonth"
internal const val CARD_EXPIRY_YEAR_FIELD_NAME = "expirationYear"

@KoinApiExtension
internal class CreditCard(
    config: PaymentMethodRemoteConfig,
    private val options: PaymentMethod.Card, // FIXME why's this here? it's unused
    encodedAsJson: JSONObject = JSONObject() // FIXME passing in a as dependency so we can test
) : PaymentMethodDescriptor(config, encodedAsJson), DIAppComponent { // FIXME why is this implementing a di component?

    private val checkoutConfig: CheckoutConfig by inject()

    // FIXME identifiers should not be needed to identify instances of a class
    override val identifier = PAYMENT_CARD_IDENTIFIER

    // FIXME static call + instantiation makes it impossible to properly test
    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = NewFragmentBehaviour(CardFormFragment::newInstance, returnToPreviousOnBack = true)

    override val type: PaymentMethodType = PaymentMethodType.FORM

    override val vaultCapability: VaultCapability = VaultCapability.SINGLE_USE_AND_VAULT

    override fun createButton(context: Context): View {
        val button = View.inflate(context, R.layout.payment_method_button_card, null)
        val text = button.findViewById<TextView>(R.id.card_preview_button_text)

        text.text = when (checkoutConfig.uxMode) {
            UXMode.CHECKOUT -> context.getString(R.string.pay_by_card)
            UXMode.ADD_PAYMENT_METHOD -> context.getString(R.string.add_card)
            else -> ""
        }

        return button
    }

    // FIXME a model should not be responsible for parsing itself into json
    override fun toPaymentInstrument(): JSONObject {
        val json = JSONObject()

        json.put(CARD_NAME_FILED_NAME, getStringValue(CARD_NAME_FILED_NAME).trim())
        json.put(
            CARD_NUMBER_FIELD_NAME,
            getStringValue(CARD_NUMBER_FIELD_NAME).replace("\\s".toRegex(), "")
        )
        json.put(CARD_CVV_FIELD_NAME, getStringValue(CARD_CVV_FIELD_NAME))

        val expiry = ExpiryDateFormatter.fromString(getStringValue(CARD_EXPIRY_FIELD_NAME))

        json.put(CARD_EXPIRY_MONTH_FIELD_NAME, expiry.getMonth())
        json.put(CARD_EXPIRY_YEAR_FIELD_NAME, expiry.getYear())

        return json
    }

    // FIXME this should not be here. a model should not be responsible for validating itself
    override fun validate(): List<SyncValidationError> {
        val errors = ArrayList<SyncValidationError>()

        val name = getSanitizedValue(CARD_NAME_FILED_NAME)

        if (name.isEmpty()) {
            errors.add(
                SyncValidationError(
                    name = CARD_NAME_FILED_NAME,
                    errorId = R.string.form_error_required,
                    fieldId = R.string.card_holder_name
                )
            )
        }

        // FIXME static call (formatter should be injected)
        val number = CardNumberFormatter.fromString(getSanitizedValue(CARD_NUMBER_FIELD_NAME))

        if (number.isEmpty()) {
            errors.add(
                SyncValidationError(
                    name = CARD_NUMBER_FIELD_NAME,
                    errorId = R.string.form_error_required,
                    fieldId = R.string.card_number
                )
            )
        } else if (!number.isValid()) {
            errors.add(
                SyncValidationError(
                    name = CARD_NUMBER_FIELD_NAME,
                    errorId = R.string.form_error_invalid,
                    fieldId = R.string.card_number
                )
            )
        }

        val cvv = getSanitizedValue(CARD_CVV_FIELD_NAME)

        if (cvv.isEmpty()) {
            errors.add(
                SyncValidationError(
                    name = CARD_CVV_FIELD_NAME,
                    errorId = R.string.form_error_required,
                    fieldId = R.string.card_cvv
                )
            )
        } else if (cvv.length != number.getCVVLength()) {
            errors.add(
                SyncValidationError(
                    name = CARD_CVV_FIELD_NAME,
                    errorId = R.string.form_error_invalid,
                    fieldId = R.string.card_cvv
                )
            )
        }

        // FIXME static call (formatter should be injected)
        val expiry = ExpiryDateFormatter.fromString(getSanitizedValue(CARD_EXPIRY_FIELD_NAME))

        if (expiry.isEmpty()) {
            errors.add(
                SyncValidationError(
                    name = CARD_EXPIRY_FIELD_NAME,
                    errorId = R.string.form_error_required,
                    fieldId = R.string.card_expiry
                )
            )
        } else if (!expiry.isValid()) {
            errors.add(
                SyncValidationError(
                    name = CARD_EXPIRY_FIELD_NAME,
                    errorId = R.string.form_error_invalid,
                    fieldId = R.string.card_expiry
                )
            )
        }

        return errors
    }

    private fun getSanitizedValue(key: String): String {
        return getStringValue(key).trim()
    }
}
