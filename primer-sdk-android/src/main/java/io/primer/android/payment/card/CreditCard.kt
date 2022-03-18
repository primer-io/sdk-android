package io.primer.android.payment.card

import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import io.primer.android.PaymentMethodIntent
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.R
import io.primer.android.PrimerSessionIntent
import io.primer.android.data.configuration.models.PaymentMethodRemoteConfig
import io.primer.android.databinding.PaymentMethodButtonCardBinding
import io.primer.android.di.DIAppComponent
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.model.SyncValidationError
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import io.primer.android.payment.utils.ButtonViewHelper.generateButtonContent
import io.primer.android.ui.CardNumberFormatter
import io.primer.android.ui.ExpiryDateFormatter
import io.primer.android.ui.fragments.CardFormFragment
import io.primer.android.utils.removeSpaces
import org.json.JSONObject
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
internal class CreditCard(
    config: PaymentMethodRemoteConfig,
    private val options: Card,
) : PaymentMethodDescriptor(config), DIAppComponent {

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

    override fun createButton(container: ViewGroup): View {
        val binding = PaymentMethodButtonCardBinding.inflate(
            LayoutInflater.from(container.context),
            container,
            false
        )

        val content = generateButtonContent(theme, container.context)
        val splash = theme.splashColor.getColor(container.context, theme.isDarkMode)
        val rippleColor = ColorStateList.valueOf(splash)
        binding.cardPreviewButton.background = RippleDrawable(rippleColor, content, null)

        val text = binding.cardPreviewButtonText
        val drawable = ContextCompat.getDrawable(
            container.context,
            R.drawable.ic_logo_credit_card
        )

        text.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)

        text.setTextColor(
            theme.paymentMethodButton.text.defaultColor.getColor(
                container.context,
                theme.isDarkMode
            )
        )

        text.text = when (checkoutConfig.paymentMethodIntent) {
            PrimerSessionIntent.CHECKOUT -> container.context.getString(R.string.pay_by_card)
            PrimerSessionIntent.VAULT ->
                container.context.getString(R.string.credit_debit_card)
        }

        val icon = text.compoundDrawables

        DrawableCompat.setTint(
            DrawableCompat.wrap(icon[0]),
            theme.paymentMethodButton.text.defaultColor.getColor(
                container.context,
                theme.isDarkMode
            )
        )

        return binding.root
    }

    // FIXME a model should not be responsible for parsing itself into json
    override fun toPaymentInstrument(): JSONObject {
        val json = JSONObject()

        json.putFor(
            PrimerInputFieldType.CARDHOLDER_NAME,
            values.valueBy(PrimerInputFieldType.CARDHOLDER_NAME).trim()
        )
        json.putFor(
            PrimerInputFieldType.CARD_NUMBER,
            values.valueBy(PrimerInputFieldType.CARD_NUMBER).removeSpaces()
        )
        json.putFor(
            PrimerInputFieldType.CVV,
            values.valueBy(PrimerInputFieldType.CVV)
        )

        val expiry = ExpiryDateFormatter.fromString(
            values.valueBy(PrimerInputFieldType.EXPIRY_DATE)
        )

        json.putFor(PrimerInputFieldType.EXPIRY_MONTH, expiry.getMonth())
        json.putFor(PrimerInputFieldType.EXPIRY_YEAR, expiry.getYear())

        return json
    }

    // FIXME this should not be here. a model should not be responsible for validating itself
    override fun validate(): List<SyncValidationError> {
        val errors = ArrayList<SyncValidationError>()

        if (hasCardholderName) {
            val name = values.valueBy(PrimerInputFieldType.CARDHOLDER_NAME)
            if (name.isEmpty()) {
                errors.add(
                    SyncValidationError(
                        name = PrimerInputFieldType.CARDHOLDER_NAME.field,
                        errorId = R.string.form_error_required,
                        fieldId = R.string.card_holder_name
                    )
                )
            }
        }

        // FIXME static call (formatter should be injected)
        val number = CardNumberFormatter.fromString(
            values.valueBy(PrimerInputFieldType.CARD_NUMBER)
        )

        if (number.isEmpty()) {
            errors.add(
                SyncValidationError(
                    name = PrimerInputFieldType.CARD_NUMBER.field,
                    errorId = R.string.form_error_required,
                    fieldId = R.string.card_number
                )
            )
        } else if (!number.isValid()) {
            errors.add(
                SyncValidationError(
                    name = PrimerInputFieldType.CARD_NUMBER.field,
                    errorId = R.string.form_error_invalid,
                    fieldId = R.string.card_number
                )
            )
        }

        val cvv = values.valueBy(PrimerInputFieldType.CVV)

        if (cvv.isEmpty()) {
            errors.add(
                SyncValidationError(
                    name = PrimerInputFieldType.CVV.field,
                    errorId = R.string.form_error_required,
                    fieldId = R.string.card_cvv
                )
            )
        } else if (cvv.length != number.getCvvLength()) {
            errors.add(
                SyncValidationError(
                    name = PrimerInputFieldType.CVV.field,
                    errorId = R.string.form_error_invalid,
                    fieldId = R.string.card_cvv
                )
            )
        }

        // FIXME static call (formatter should be injected)
        val expiry = ExpiryDateFormatter.fromString(
            values.valueBy(PrimerInputFieldType.EXPIRY_DATE)
        )

        if (expiry.isEmpty()) {
            errors.add(
                SyncValidationError(
                    name = PrimerInputFieldType.EXPIRY_DATE.field,
                    errorId = R.string.form_error_required,
                    fieldId = R.string.card_expiry
                )
            )
        } else if (!expiry.isValid()) {
            errors.add(
                SyncValidationError(
                    name = PrimerInputFieldType.EXPIRY_DATE.field,
                    errorId = R.string.form_error_invalid,
                    fieldId = R.string.card_expiry
                )
            )
        }

        if (hasPostalCode) {
            val postalCode = values.valueBy(PrimerInputFieldType.POSTAL_CODE)

            if (postalCode.isEmpty()) {
                errors.add(
                    SyncValidationError(
                        name = PrimerInputFieldType.POSTAL_CODE.field,
                        errorId = R.string.form_error_required,
                        fieldId = R.string.card_zip
                    )
                )
            }
        }

        return errors
    }

    override fun getValidAutoFocusableFields(): Set<String> {
        val fields = hashSetOf<String>()
        val number = CardNumberFormatter.fromString(
            values.valueBy(PrimerInputFieldType.CARD_NUMBER)
        )
        if (number.isValid() && number.getMaxLength() == number.getValue().length) {
            fields.add(PrimerInputFieldType.CARD_NUMBER.field)
        }

        val cvv = values.valueBy(PrimerInputFieldType.CVV)
        if (cvv.length == number.getCvvLength()) fields.add(PrimerInputFieldType.CVV.field)

        val expiry = ExpiryDateFormatter.fromString(
            values.valueBy(PrimerInputFieldType.EXPIRY_DATE)
        )
        if (expiry.isValid()) fields.add(PrimerInputFieldType.EXPIRY_DATE.field)

        return fields
    }
}
