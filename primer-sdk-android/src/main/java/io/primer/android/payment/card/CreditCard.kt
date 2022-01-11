package io.primer.android.payment.card

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import io.primer.android.PrimerTheme
import io.primer.android.R
import io.primer.android.PaymentMethodIntent
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
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
internal const val CARD_POSTAL_CODE_FIELD_NAME = "postalCode"
internal const val CARD_EXPIRY_MONTH_FIELD_NAME = "expirationMonth"
internal const val CARD_EXPIRY_YEAR_FIELD_NAME = "expirationYear"

@KoinApiExtension
internal class CreditCard(
    config: PaymentMethodRemoteConfig,
    private val options: Card,
    encodedAsJson: JSONObject = JSONObject(), // FIXME passing in a as dependency so we can test
) : PaymentMethodDescriptor(config, encodedAsJson), DIAppComponent {

    private val checkoutConfig: PrimerConfig by inject()
    private val theme: PrimerTheme by inject()

    var hasPostalCode: Boolean = false
    var hasCardholderName: Boolean = true

    // FIXME static call + instantiation makes it impossible to properly test
    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = NewFragmentBehaviour(CardFormFragment::newInstance, returnToPreviousOnBack = true)

    override val behaviours: List<SelectedPaymentMethodBehaviour> = emptyList()

    override val type: PaymentMethodUiType = PaymentMethodUiType.FORM

    override val vaultCapability: VaultCapability = VaultCapability.SINGLE_USE_AND_VAULT

    private fun generateButtonContent(context: Context): GradientDrawable {
        val content = GradientDrawable()
        val strokeColor = theme.paymentMethodButton.border.defaultColor
            .getColor(context, theme.isDarkMode)
        val width = theme.paymentMethodButton.border.width.getPixels(context)
        content.setStroke(width, strokeColor)
        content.cornerRadius = theme.paymentMethodButton.cornerRadius.getDimension(context)
        content.color = ColorStateList
            .valueOf(theme.paymentMethodButton.defaultColor.getColor(context, theme.isDarkMode))
        return content
    }

    override fun createButton(container: ViewGroup): View {
        val button = LayoutInflater.from(container.context).inflate(
            R.layout.payment_method_button_card,
            container,
            false
        )

        val content = generateButtonContent(container.context)
        val splash = theme.splashColor.getColor(container.context, theme.isDarkMode)
        val rippleColor = ColorStateList.valueOf(splash)
        button.background = RippleDrawable(rippleColor, content, null)

        val text = button.findViewById<TextView>(R.id.card_preview_button_text)
        val drawable = ContextCompat.getDrawable(
            container.context,
            R.drawable.credit_card_icon
        )

        text.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)

        text.setTextColor(
            theme.paymentMethodButton.text.defaultColor.getColor(
                container.context,
                theme.isDarkMode
            )
        )

        text.text = when (checkoutConfig.paymentMethodIntent) {
            PaymentMethodIntent.CHECKOUT -> container.context.getString(R.string.pay_by_card)
            PaymentMethodIntent.VAULT -> container.context.getString(R.string.credit_debit_card)
        }

        val icon = text.compoundDrawables

        DrawableCompat.setTint(
            DrawableCompat.wrap(icon[0]),
            theme.paymentMethodButton.text.defaultColor.getColor(
                container.context,
                theme.isDarkMode
            )
        )

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

        if (hasCardholderName) {
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
        } else if (cvv.length != number.getCvvLength()) {
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

        if (hasPostalCode) {
            val postalCode = getSanitizedValue(CARD_POSTAL_CODE_FIELD_NAME)

            if (postalCode.isEmpty()) {
                errors.add(
                    SyncValidationError(
                        name = CARD_POSTAL_CODE_FIELD_NAME,
                        errorId = R.string.form_error_required,
                        fieldId = R.string.card_zip
                    )
                )
            }
        }

        return errors
    }

    private fun getSanitizedValue(key: String): String {
        return getStringValue(key).trim()
    }
}
