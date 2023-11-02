package io.primer.android.components.domain.payments.paymentMethods.nolpay.repository

internal interface NolPayCompletePaymentRepository {

    suspend fun completePayment(completeUrl: String): Result<Unit>
}
