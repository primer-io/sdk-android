package io.primer.android.completion

interface PrimerPaymentCreationDecisionHandler {
    /**
     * Continue with payment creation.
     *
     * @param idempotencyKey Optional idempotency key to prevent duplicate payments.
     * If provided, the SDK will attach this as the X-Idempotency-Key header on POST /payments
     * and POST /payments/{id}/resume requests. The same key will be used for the entire payment
     * lifecycle (create + all resumes).
     */
    fun continuePaymentCreation(idempotencyKey: String? = null)

    fun abortPaymentCreation(errorMessage: String?)
}
