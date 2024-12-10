package io.primer.android.payments.core.tokenization.presentation.composable

interface PaymentMethodTokenizationCollectorParams

object NoOpPaymentMethodTokenizationCollectorParams : PaymentMethodTokenizationCollectorParams

interface PaymentMethodTokenizationCollectorDelegate<in P : PaymentMethodTokenizationCollectorParams> {

    suspend fun startDataCollection(params: P): Result<Unit>
}
