package io.primer.android.paymentmethods

/**
 * A PaymentMethodChecker is responsible for evaluating if a given [PaymentMethod] is available or
 * not, at run-time. See [PaymentMethodCheckerRegistry].
 */
fun interface PaymentMethodChecker {

    suspend fun shouldPaymentMethodBeAvailable(
        paymentMethod: PaymentMethod
    ): Boolean
}
