package io.primer.android.domain.error.models

import io.primer.android.analytics.domain.models.BaseContextParams
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.data.configuration.models.PaymentMethodType
import java.util.UUID

internal sealed class StripeError : PrimerError() {

    data object StripeInvalidPublishableKeyError : StripeError()

    data class StripeSdkError(
        val message: String
    ) : StripeError()

    override val errorId: String
        get() = when (this) {
            is StripeInvalidPublishableKeyError -> "stripe-invalid-publishable-key"
            is StripeSdkError -> "stripe-sdk-error"
        }

    override val description: String
        get() = when (this) {
            is StripeInvalidPublishableKeyError ->
                "Publishable key is invalid"
            is StripeSdkError ->
                "Multiple errors occurred: $message"
        }

    override val diagnosticsId: String
        get() = UUID.randomUUID().toString()

    override val errorCode: String? = null

    override val exposedError: PrimerError
        get() = this

    override val recoverySuggestion: String?
        get() = null

    override val context: BaseContextParams get() =
        ErrorContextParams(errorId, PaymentMethodType.STRIPE_ACH.name)
}
