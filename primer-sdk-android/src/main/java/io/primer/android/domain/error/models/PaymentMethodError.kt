package io.primer.android.domain.error.models

import io.primer.android.PaymentMethodIntent
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.internal.PrimerPaymentMethod
import java.util.UUID

internal sealed class PaymentMethodError : PrimerError() {

    class MisConfiguredPaymentMethodError(val primerPaymentMethod: PrimerPaymentMethod) :
        PaymentMethodError() {
        override val exposedError = this
    }

    class PaymentMethodCancelledError(
        val paymentMethodType: PaymentMethodType,
    ) : PaymentMethodError() {
        override val exposedError = this
    }

    class UnsupportedIntentPaymentMethodError(
        val paymentMethodType: PrimerPaymentMethod,
        val intent: PaymentMethodIntent
    ) : PaymentMethodError() {
        override val exposedError = this
    }

    override val errorId: String
        get() = when (this) {
            is MisConfiguredPaymentMethodError -> "misconfigured-payment-method"
            is PaymentMethodCancelledError -> "cancelled"
            is UnsupportedIntentPaymentMethodError -> "unsupported-intent"
        }

    override val description: String
        get() = when (this) {
            is MisConfiguredPaymentMethodError ->
                "Cannot present $primerPaymentMethod because it has not been configured correctly."
            is PaymentMethodCancelledError ->
                "Vaulting/Checking out for $paymentMethodType was cancelled by the user."
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
            is UnsupportedIntentPaymentMethodError ->
                "Use a different payment method for $intent," +
                    " or the same payment method with ${intent.oppositeIntent}."
        }
}
