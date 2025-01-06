package io.primer.android.components.implementation.errors.domain.model

import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.domain.models.BaseContextParams
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.domain.error.models.PrimerError
import java.util.UUID

internal sealed class PaymentMethodConfigurationError : PrimerError() {
    class MisConfiguredPaymentMethodError(val paymentMethodType: String) :
        PaymentMethodConfigurationError() {
        override val exposedError = this

        override val context: BaseContextParams
            get() =
                ErrorContextParams(errorId, paymentMethodType)
    }

    class UnsupportedPaymentMethodError(val paymentMethodType: String) :
        PaymentMethodConfigurationError() {
        override val exposedError = this

        override val context: BaseContextParams
            get() =
                ErrorContextParams(errorId, paymentMethodType)
    }

    class UnsupportedIntentPaymentMethodError(
        val paymentMethodType: String,
        val intent: PrimerSessionIntent,
    ) : PaymentMethodConfigurationError() {
        override val exposedError = this

        override val context: BaseContextParams
            get() =
                ErrorContextParams(errorId, paymentMethodType)
    }

    override val errorId: String
        get() =
            when (this) {
                is MisConfiguredPaymentMethodError -> "misconfigured-payment-method"
                is UnsupportedPaymentMethodError -> "unsupported-payment-method-type"
                is UnsupportedIntentPaymentMethodError -> "unsupported-session-intent"
            }

    override val description: String
        get() =
            when (this) {
                is MisConfiguredPaymentMethodError ->
                    "Cannot present $paymentMethodType because it has not been configured correctly."

                is UnsupportedPaymentMethodError ->
                    "Cannot present $paymentMethodType because it is not supported."

                is UnsupportedIntentPaymentMethodError ->
                    "Cannot initialize the SDK because $paymentMethodType does not support $intent."
            }

    override val errorCode: String? = null

    override val diagnosticsId = UUID.randomUUID().toString()

    override val recoverySuggestion: String?
        get() =
            when (this) {
                is MisConfiguredPaymentMethodError ->
                    "Ensure that $paymentMethodType has been configured correctly " +
                        "on the dashboard (https://dashboard.primer.io/)"

                is UnsupportedPaymentMethodError -> null
                is UnsupportedIntentPaymentMethodError ->
                    "Use a different payment method for $intent," +
                        " or the same payment method with ${intent.oppositeIntent}."
            }
}
