package io.primer.android.domain.error.models

import io.primer.android.data.payments.create.models.PaymentStatus
import java.util.UUID

internal sealed class PaymentError : PrimerError() {

    class PaymentFailedError(val paymentId: String, val paymentStatus: PaymentStatus) :
        PaymentError()

    class PaymentCreateFailedError(
        val serverDescription: String,
        val serverDiagnosticsId: String?
    ) : PaymentError()

    class PaymentResumeFailedError(
        val serverDescription: String,
        val serverDiagnosticsId: String?
    ) : PaymentError()

    override val errorId: String
        get() = when (this) {
            is PaymentFailedError -> "payment-failed"
            is PaymentCreateFailedError -> "failed-to-create-payment"
            is PaymentResumeFailedError -> "failed-to-resume-payment"
        }

    override val description: String
        get() = when (this) {
            is PaymentFailedError ->
                "The payment with id $paymentId was created but ended up in a " +
                    "$paymentStatus status."
            is PaymentCreateFailedError -> serverDescription
            is PaymentResumeFailedError -> serverDescription
        }

    override val diagnosticsId: String
        get() = when (this) {
            is PaymentFailedError -> UUID.randomUUID().toString()
            is PaymentCreateFailedError -> serverDiagnosticsId ?: UUID.randomUUID().toString()
            is PaymentResumeFailedError -> serverDiagnosticsId ?: UUID.randomUUID().toString()
        }

    override val exposedError: PrimerError
        get() = this

    override val recoverySuggestion: String?
        get() = when (this) {
            is PaymentFailedError -> null
            is PaymentCreateFailedError ->
                "Contact Primer and provide us with diagnostics id $diagnosticsId"
            is PaymentResumeFailedError ->
                "Contact Primer and provide us with diagnostics id $diagnosticsId"
        }
}
