package io.primer.android.domain.error.models

import io.primer.android.PrimerSessionIntent
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.configuration.models.PrimerPaymentMethodType
import java.util.UUID

internal sealed class PaymentMethodError : PrimerError() {

    class MisConfiguredPaymentMethodError(val primerPaymentMethod: PrimerPaymentMethodType) :
        PaymentMethodError() {
        override val exposedError = this
    }

    class PaymentMethodCancelledError(
        val paymentMethodType: PaymentMethodType,
    ) : PaymentMethodError() {
        override val exposedError = this
    }

    class UnsupportedPaymentMethodError(val primerPaymentMethod: PrimerPaymentMethodType) :
        PaymentMethodError() {
        override val exposedError = this
    }

    class UnsupportedIntentPaymentMethodError(
        val paymentMethodType: PrimerPaymentMethodType,
        val intent: PrimerSessionIntent
    ) : PaymentMethodError() {
        override val exposedError = this
    }

    override val errorId: String
        get() = when (this) {
            is MisConfiguredPaymentMethodError -> "misconfigured-payment-method"
            is PaymentMethodCancelledError -> "cancelled"
            is UnsupportedPaymentMethodError -> "unsupported-payment-method"
            is UnsupportedIntentPaymentMethodError -> "unsupported-intent"
        }

    override val description: String
        get() = when (this) {
            is MisConfiguredPaymentMethodError ->
                "Cannot present $primerPaymentMethod because it has not been configured correctly."
            is PaymentMethodCancelledError ->
                "Vaulting/Checking out for $paymentMethodType was cancelled by the user."
            is UnsupportedPaymentMethodError ->
                "Cannot present $primerPaymentMethod because it is not supported."
            is UnsupportedIntentPaymentMethodError ->
                "Cannot initialize the SDK because $paymentMethodType does not support $intent."
        }

    override val diagnosticsId = UUID.randomUUID().toString()

    override val recoverySuggestion: String?
        get() = when (this) {
            is MisConfiguredPaymentMethodError ->
                "Ensure that $primerPaymentMethod has been configured correctly " +
                    "on the dashboard (https://dashboard.primer.io/)"
            is PaymentMethodCancelledError -> null
            is UnsupportedPaymentMethodError -> null
            is UnsupportedIntentPaymentMethodError ->
                "Use a different payment method for $intent," +
                    " or the same payment method with ${intent.oppositeIntent}."
        }
}
