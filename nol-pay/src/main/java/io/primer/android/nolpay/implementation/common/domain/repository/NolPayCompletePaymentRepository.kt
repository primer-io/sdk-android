package io.primer.android.nolpay.implementation.common.domain.repository

internal fun interface NolPayCompletePaymentRepository {

    suspend fun completePayment(completeUrl: String): Result<Unit>
}
