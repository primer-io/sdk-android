package io.primer.android.payment.card

import io.primer.android.R
import io.primer.android.components.domain.core.mapper.PrimerPaymentMethodRawDataMapper
import io.primer.android.components.domain.core.mapper.card.CardRawDataMapper
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.inputs.models.putFor
import io.primer.android.components.domain.inputs.models.valueBy
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.di.DIAppComponent
import io.primer.android.model.SyncValidationError
import io.primer.android.payment.HeadlessDefinition
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import io.primer.android.ui.CardNumberFormatter
import io.primer.android.ui.ExpiryDateFormatter
import io.primer.android.ui.fragments.CardFormFragment
import io.primer.android.utils.removeSpaces
import org.json.JSONObject

internal class CreditCard(
    private val localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse,
) : PaymentMethodDescriptor(config), DIAppComponent {

    var availableFields = mutableMapOf<PrimerInputElementType, Boolean>()

    // FIXME static call + instantiation makes it impossible to properly test
    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = NewFragmentBehaviour(
            CardFormFragment::newInstance,
            true
            // localConfig.isStandalonePaymentMethod.not()
        )

    override val behaviours: List<SelectedPaymentMethodBehaviour> = emptyList()

    override val type: PaymentMethodUiType = PaymentMethodUiType.FORM

    override val vaultCapability: VaultCapability = VaultCapability.SINGLE_USE_AND_VAULT

    // FIXME a model should not be responsible for parsing itself into json
    override fun toPaymentInstrument(): JSONObject {
        val json = JSONObject()

        json.putFor(
            PrimerInputElementType.CARDHOLDER_NAME,
            values.valueBy(PrimerInputElementType.CARDHOLDER_NAME).trim()
        )
        json.putFor(
            PrimerInputElementType.CARD_NUMBER,
            values.valueBy(PrimerInputElementType.CARD_NUMBER).removeSpaces()
        )
        json.putFor(
            PrimerInputElementType.CVV,
            values.valueBy(PrimerInputElementType.CVV)
        )

        val expiry = ExpiryDateFormatter.fromString(
            values.valueBy(PrimerInputElementType.EXPIRY_DATE)
        )

        json.putFor(PrimerInputElementType.EXPIRY_MONTH, expiry.getMonth())
        json.putFor(PrimerInputElementType.EXPIRY_YEAR, expiry.getYear())

        return json
    }

    // FIXME this should not be here. a model should not be responsible for validating itself
    override fun validate(): List<SyncValidationError> {
        val errors = ArrayList<SyncValidationError>()

        if (availableFields[PrimerInputElementType.CARDHOLDER_NAME] == null ||
            availableFields[PrimerInputElementType.CARDHOLDER_NAME] == true
        ) {
            val name = values.valueBy(PrimerInputElementType.CARDHOLDER_NAME)
            if (name.isEmpty()) {
                errors.add(
                    SyncValidationError(
                        name = PrimerInputElementType.CARDHOLDER_NAME.field,
                        errorFormatId = R.string.form_error_required,
                        fieldId = R.string.card_holder_name
                    )
                )
            }
        }

        // FIXME static call (formatter should be injected)
        val number = CardNumberFormatter.fromString(
            values.valueBy(PrimerInputElementType.CARD_NUMBER)
        )

        if (number.isEmpty()) {
            errors.add(
                SyncValidationError(
                    name = PrimerInputElementType.CARD_NUMBER.field,
                    errorFormatId = R.string.form_error_required,
                    fieldId = R.string.card_number
                )
            )
        } else if (!number.isValid()) {
            errors.add(
                SyncValidationError(
                    name = PrimerInputElementType.CARD_NUMBER.field,
                    errorFormatId = R.string.form_error_invalid,
                    fieldId = R.string.card_number
                )
            )
        }

        val cvv = values.valueBy(PrimerInputElementType.CVV)

        if (cvv.isEmpty()) {
            errors.add(
                SyncValidationError(
                    name = PrimerInputElementType.CVV.field,
                    errorFormatId = R.string.form_error_required,
                    fieldId = R.string.card_cvv
                )
            )
        } else if (cvv.length != number.getCvvLength()) {
            errors.add(
                SyncValidationError(
                    name = PrimerInputElementType.CVV.field,
                    errorFormatId = R.string.form_error_invalid,
                    fieldId = R.string.card_cvv
                )
            )
        }

        val expiry = ExpiryDateFormatter.fromString(
            values.valueBy(PrimerInputElementType.EXPIRY_DATE)
        )

        if (expiry.isEmpty()) {
            errors.add(
                SyncValidationError(
                    name = PrimerInputElementType.EXPIRY_DATE.field,
                    errorFormatId = R.string.form_error_required,
                    fieldId = R.string.card_expiry
                )
            )
        } else if (!expiry.isValid()) {
            errors.add(
                SyncValidationError(
                    name = PrimerInputElementType.EXPIRY_DATE.field,
                    errorFormatId = R.string.form_error_invalid,
                    fieldId = R.string.card_expiry
                )
            )
        }

        return errors
    }

    override fun getValidAutoFocusableFields(): Set<String> {
        val fields = hashSetOf<String>()
        val number = CardNumberFormatter.fromString(
            values.valueBy(PrimerInputElementType.CARD_NUMBER)
        )
        if (number.isValid() && number.getMaxLength() == number.getValue().length) {
            fields.add(PrimerInputElementType.CARD_NUMBER.field)
        }

        val cvv = values.valueBy(PrimerInputElementType.CVV)
        if (cvv.length == number.getCvvLength()) fields.add(PrimerInputElementType.CVV.field)

        val expiry = ExpiryDateFormatter.fromString(
            values.valueBy(PrimerInputElementType.EXPIRY_DATE)
        )
        if (expiry.isValid()) fields.add(PrimerInputElementType.EXPIRY_DATE.field)

        val containsCardholderName =
            availableFields[PrimerInputElementType.CARDHOLDER_NAME] == null ||
                availableFields[PrimerInputElementType.CARDHOLDER_NAME] == true
        if (containsCardholderName &&
            values.valueBy(PrimerInputElementType.CARDHOLDER_NAME).isNotBlank()
        ) fields.add(PrimerInputElementType.CARDHOLDER_NAME.field)

        return fields
    }

    override val headlessDefinition: HeadlessDefinition
        get() = HeadlessDefinition(
            listOf(
                PrimerPaymentMethodManagerCategory.RAW_DATA,
                PrimerPaymentMethodManagerCategory.CARD_COMPONENTS
            ),
            HeadlessDefinition.RawDataDefinition(
                PrimerCardData::class,
                CardRawDataMapper(config) as PrimerPaymentMethodRawDataMapper<PrimerRawData>
            )
        )
}
