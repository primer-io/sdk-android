package io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.error

import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.error.models.StripeError
import io.primer.android.stripe.exceptions.StripePublishableKeyMismatchException
import io.primer.android.stripe.exceptions.StripeSdkException

internal class StripeErrorMapper : DefaultErrorMapper() {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is StripePublishableKeyMismatchException -> StripeError.StripeInvalidPublishableKeyError
            is StripeSdkException -> StripeError.StripeSdkError(throwable.message.orEmpty())
            else -> return super.getPrimerError(throwable)
        }
    }
}
