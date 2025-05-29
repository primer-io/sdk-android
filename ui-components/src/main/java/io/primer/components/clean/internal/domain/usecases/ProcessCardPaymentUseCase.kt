package io.primer.components.clean.internal.domain.usecases

import io.primer.components.clean.internal.domain.models.Card
import io.primer.components.clean.internal.domain.models.Payment


/**
 * Use case for processing card payments.
 * Contains business logic for payment processing.
 */
internal class ProcessCardPaymentUseCase(
    private val paymentRepository: PaymentRepository
) {
    
    suspend operator fun invoke(
        card: Card,
        amount: String,
        currency: String
    ): Result<Payment> {
        
        // Business logic validation
        if (!card.isValid()) {
            return Result.failure(IllegalArgumentException("Invalid card details"))
        }
        
        if (amount.toBigDecimalOrNull() == null || amount.toBigDecimal() <= 0.toBigDecimal()) {
            return Result.failure(IllegalArgumentException("Invalid amount"))
        }
        
        if (currency.length != 3) {
            return Result.failure(IllegalArgumentException("Invalid currency code"))
        }
        
        // Delegate to repository for actual processing
        return paymentRepository.processCardPayment(card, amount, currency)
    }
}
