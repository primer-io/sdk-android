package io.primer.android.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.primer.android.R
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManager
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerInterface
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerListener
import io.primer.android.model.SyncValidationError
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import java.util.Collections

internal class CardViewModel : ViewModel() {

    private val cardManager: PrimerHeadlessUniversalCheckoutRawDataManagerInterface by lazy {
        PrimerHeadlessUniversalCheckoutRawDataManager.newInstance(PaymentMethodType.PAYMENT_CARD.name)
    }

    private val _cardValidationErrors: MutableLiveData<List<SyncValidationError>> = MutableLiveData()
    val cardValidationErrors: LiveData<List<SyncValidationError>> = _cardValidationErrors

    private val _billingAddressValidationErrors: MutableLiveData<List<SyncValidationError>> = MutableLiveData()
    val billingAddressValidationErrors: LiveData<List<SyncValidationError>> = _billingAddressValidationErrors

    val tokenizationStatus = MutableLiveData(TokenizationStatus.NONE)
    val submitted = MutableLiveData(false)
    val autoFocusFields: MutableLiveData<Set<PrimerInputElementType>> = MutableLiveData(
        Collections.emptySet()
    )

    private var cardData: PrimerCardData? = null

    override fun onCleared() {
        super.onCleared()
        cardManager.cleanup()
    }

    fun initialize() {
        cardManager.setListener(object : PrimerHeadlessUniversalCheckoutRawDataManagerListener {
            override fun onValidationChanged(isValid: Boolean, errors: List<PrimerInputValidationError>) {
                _cardValidationErrors.value = errors.map { inputValidationError ->
                    inputValidationError.toSyncValidationError(cardData)
                }
                autoFocusFields.postValue(getValidAutoFocusableFields(errors))
            }
        })
    }

    fun getRequiredElementTypes() = cardManager.getRequiredInputElementTypes()

    fun onCardDataChanged(cardData: PrimerCardData) = cardManager.setRawData(cardData).also {
        this.cardData = cardData
    }

    fun submit() = cardManager.submit().also {
        submitted.postValue(true)
    }

    fun isValid() = _cardValidationErrors.value.isNullOrEmpty() &&
        _billingAddressValidationErrors.value.isNullOrEmpty() &&
        cardData != null &&
        isSubmitButtonEnabled(tokenizationStatus.value)

    fun updateValidationErrors(errors: List<SyncValidationError>) {
        _billingAddressValidationErrors.value = errors
    }

    private fun isSubmitButtonEnabled(tokenizationStatus: TokenizationStatus?): Boolean {
        return tokenizationStatus == TokenizationStatus.NONE ||
            tokenizationStatus == TokenizationStatus.ERROR
    }

    private fun getValidAutoFocusableFields(errors: List<PrimerInputValidationError>): Set<PrimerInputElementType> {
        val fields = hashSetOf<PrimerInputElementType>()
        if (errors.none { it.inputElementType == PrimerInputElementType.CARD_NUMBER }) {
            fields.add(PrimerInputElementType.CARD_NUMBER)
        }

        if (errors.none { it.inputElementType == PrimerInputElementType.CVV }) {
            fields.add(PrimerInputElementType.CVV)
        }

        if (errors.none { it.inputElementType == PrimerInputElementType.EXPIRY_DATE }) {
            fields.add(PrimerInputElementType.EXPIRY_DATE)
        }

        val containsCardholderName =
            getRequiredElementTypes().contains(PrimerInputElementType.CARDHOLDER_NAME)
        if (containsCardholderName && cardData?.cardHolderName.isNullOrBlank().not()
        ) {
            fields.add(PrimerInputElementType.CARDHOLDER_NAME)
        }

        return fields
    }
}

internal fun PrimerInputValidationError.toSyncValidationError(cardData: PrimerCardData?) = when (errorId) {
    "invalid-card-number" -> SyncValidationError(
        inputElementType = inputElementType,
        errorId = errorId,
        errorFormatId = if (cardData?.cardNumber.isNullOrBlank()) {
            R.string.form_error_required
        } else {
            R.string.form_error_invalid
        },
        fieldId = R.string.card_number
    )

    "unsupported-card-type" -> SyncValidationError(
        inputElementType = inputElementType,
        errorId = errorId,
        errorFormatId = if (cardData?.cardNumber.isNullOrBlank()) {
            R.string.form_error_required
        } else {
            R.string.form_error_card_type_not_supported
        },
        fieldId = R.string.card_number
    )

    "invalid-cvv" -> SyncValidationError(
        inputElementType = inputElementType,
        errorId = errorId,
        errorFormatId = if (cardData?.cvv.isNullOrBlank()) {
            R.string.form_error_required
        } else {
            R.string.form_error_invalid
        },
        fieldId = R.string.card_cvv
    )

    "invalid-expiry-date" -> SyncValidationError(
        inputElementType = inputElementType,
        errorId = errorId,
        errorFormatId = if (cardData?.expiryDate.isNullOrBlank()) {
            R.string.form_error_required
        } else {
            R.string.form_error_invalid
        },
        fieldId = R.string.card_expiry
    )

    "invalid-cardholder-name" -> SyncValidationError(
        inputElementType = inputElementType,
        errorId = errorId,
        errorFormatId = if (cardData?.cardHolderName.isNullOrBlank()) {
            R.string.form_error_required
        } else {
            R.string.form_error_card_holder_name_length
        },
        fieldId = R.string.card_holder_name
    )

    else -> error("Unsupported error id mapping for $errorId")
}
