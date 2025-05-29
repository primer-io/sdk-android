package io.primer.components.clean.internal.domain.usecases

import io.primer.components.clean.internal.domain.models.Card

/**
 * Use case for validating card details.
 * Contains business logic for card validation.
 */
internal class ValidateCardUseCase(
    private val paymentRepository: PaymentRepository
) {
    
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<ValidationError>
    ) {
        enum class ValidationError {
            INVALID_CARD_NUMBER,
            INVALID_EXPIRY_DATE,
            INVALID_CVV,
            INVALID_HOLDER_NAME,
            CARD_EXPIRED
        }
    }
    
    suspend operator fun invoke(card: Card): ValidationResult {
        val errors = mutableListOf<ValidationResult.ValidationError>()
        
        // Local validation first
        if (!isValidCardNumber(card.number)) {
            errors.add(ValidationResult.ValidationError.INVALID_CARD_NUMBER)
        }
        
        if (!isValidExpiry(card.expiryMonth, card.expiryYear)) {
            errors.add(ValidationResult.ValidationError.INVALID_EXPIRY_DATE)
        }
        
        if (isExpired(card.expiryMonth, card.expiryYear)) {
            errors.add(ValidationResult.ValidationError.CARD_EXPIRED)
        }
        
        if (!isValidCvv(card.cvv)) {
            errors.add(ValidationResult.ValidationError.INVALID_CVV)
        }
        
        if (card.holderName.isBlank()) {
            errors.add(ValidationResult.ValidationError.INVALID_HOLDER_NAME)
        }
        
        // If local validation passes, check with remote
        val isLocallyValid = errors.isEmpty()
        val isRemotelyValid = if (isLocallyValid) {
            paymentRepository.validateCard(card).getOrDefault(false)
        } else false
        
        return ValidationResult(
            isValid = isLocallyValid && isRemotelyValid,
            errors = errors
        )
    }
    
    private fun isValidCardNumber(number: String): Boolean {
        val cleaned = number.replace(" ", "")
        return cleaned.length >= 13 && cleaned.all { it.isDigit() }
    }
    
    private fun isValidExpiry(month: Int, year: Int): Boolean {
        return month in 1..12 && year >= getCurrentYear()
    }
    
    private fun isExpired(month: Int, year: Int): Boolean {
        val currentYear = getCurrentYear()
        val currentMonth = getCurrentMonth()
        return year < currentYear || (year == currentYear && month < currentMonth)
    }
    
    private fun isValidCvv(cvv: String): Boolean {
        return cvv.length in 3..4 && cvv.all { it.isDigit() }
    }
    
    private fun getCurrentYear(): Int {
        return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    }
    
    private fun getCurrentMonth(): Int {
        return java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
    }
}
