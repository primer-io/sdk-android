package io.primer.android.ui.fragments.bancontact

import io.primer.android.R
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.presentation.base.BaseViewModel
import io.primer.android.ui.CardNumberFormatter
import io.primer.android.ui.ExpiryDateFormatter
import io.primer.android.utils.removeSpaces

internal class BancontactCardViewModel(analyticsInteractor: AnalyticsInteractor) :
    BaseViewModel(analyticsInteractor) {

    private val inputStates: MutableMap<String, String> = mutableMapOf()

    fun collectData() = inputStates.map { it.key to it.value }

    fun onUpdateCardNumberInput(cardNumber: String): Int? {
        inputStates[PrimerInputElementType.CARD_NUMBER.field] = cardNumber.removeSpaces()

        val numberFormatted = CardNumberFormatter.fromString(cardNumber)
        return when {
            numberFormatted.isEmpty() -> R.string.form_error_required
            !numberFormatted.isValid() -> R.string.form_error_invalid
            else -> null
        }
    }

    fun onUpdateCardExpiry(expiry: String): Int? {
        if (expiry.contains("/")) {
            val dates = expiry.split("/")
            if (dates.size > 1) {
                val month = dates[0]
                val year = dates[1]
                if (month.isNotBlank()) {
                    inputStates[PrimerInputElementType.EXPIRY_MONTH.field] =
                        String.format("%02d", month.toInt())
                }
                if (year.isNotBlank()) {
                    inputStates[PrimerInputElementType.EXPIRY_YEAR.field] =
                        String.format("%d", (CENTURY_YEARS + year.toInt()))
                }
            }
        }

        val expiryFormatted = ExpiryDateFormatter.fromString(expiry)
        return when {
            expiryFormatted.isEmpty() -> R.string.form_error_required
            !expiryFormatted.isValid() -> R.string.form_error_invalid
            else -> null
        }
    }

    fun onUpdateCardholderName(cardholderName: String): Int? {
        if (cardholderName.isNotBlank()) {
            inputStates[PrimerInputElementType.CARDHOLDER_NAME.field] = cardholderName
        }
        return when {
            cardholderName.isEmpty() -> R.string.form_error_required
            else -> null
        }
    }

    companion object {
        private const val CENTURY_YEARS = 2000
    }
}
