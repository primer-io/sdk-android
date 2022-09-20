package io.primer.android.ui.fragments.bancontact

import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.presentation.base.BaseViewModel
import io.primer.android.utils.removeSpaces

internal class BancontactCardViewModel(analyticsInteractor: AnalyticsInteractor) :
    BaseViewModel(analyticsInteractor) {

    private val inputStates: MutableMap<String, String> = mutableMapOf()

    fun collectData() = inputStates.map { it.key to it.value }

    fun onUpdateCardNumberInput(cardNumber: String): Boolean {
        inputStates["number"] = cardNumber.removeSpaces()

        // validation
        return true
    }

    fun onUpdateCardExpiry(expiry: String): Boolean {
        if (expiry.contains("/")) {
            val dates = expiry.split("/")
            if (dates.size > 1) {
                val month = dates[0]
                val year = dates[1]
                if (month.isNotBlank()) {
                    inputStates["expirationMonth"] = String.format("%02d", month.toInt())
                }
                if (year.isNotBlank()) {
                    inputStates["expirationYear"] =
                        String.format("%d", (CENTURY_YEARS + year.toInt()))
                }
            }
        }
        // validation
        return true
    }

    fun onUpdateCardholderName(cardholderName: String): Boolean {
        if (cardholderName.isNotBlank()) {
            inputStates["cardholderName"] = cardholderName
        }
        // validation
        return true
    }

    companion object {
        private const val CENTURY_YEARS = 2000
    }
}
