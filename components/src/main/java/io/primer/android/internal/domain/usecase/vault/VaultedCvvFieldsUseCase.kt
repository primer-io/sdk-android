package io.primer.android.internal.domain.usecase.vault

import io.primer.cardShared.CardNumberFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private const val DEFAULT_CVV_LENGTH = 3

/**
 * Use case for managing CVV field state and validation for vaulted payment methods.
 * Provides real-time validation using card network detection from BIN (first 6 digits).
 */
internal class VaultedCvvFieldsUseCase {

    private val _cvvValue = MutableStateFlow("")
    private val _first6Digits = MutableStateFlow("")

    /**
     * Current CVV value entered by the user.
     */
    val cvvValue: StateFlow<String> = _cvvValue.asStateFlow()

    /**
     * Expected CVV length based on the card network detected from first 6 digits.
     * AMEX cards require 4 digits, all others require 3 digits.
     */
    val expectedCvvLength: Flow<Int> = _first6Digits.map { digits ->
        if (digits.isNotEmpty()) {
            CardNumberFormatter.fromString(digits).getCvvLength()
        } else {
            DEFAULT_CVV_LENGTH // when no card network is detected
        }
    }

    /**
     * Whether the current CVV value is valid based on expected length and format.
     */
    val isCvvValid: Flow<Boolean> = combine(cvvValue, expectedCvvLength) { cvv, length ->
        cvv.length == length && cvv.all { it.isDigit() }
    }

    /**
     * Updates the CVV value.
     * @param cvv The new CVV value to set
     */
    fun updateCvv(cvv: String) {
        _cvvValue.value = cvv
    }

    /**
     * Updates the first 6 digits of the card number (BIN) used for network detection.
     * This determines the expected CVV length.
     * @param digits The first 6 digits of the card number
     */
    fun updateFirst6Digits(digits: String) {
        _first6Digits.value = digits
    }

    /**
     * Clears the CVV value, typically called when returning to selection screen
     * or when a different payment method is selected.
     */
    fun clear() {
        _cvvValue.value = ""
    }
}
