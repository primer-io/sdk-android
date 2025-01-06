package io.primer.android.stripe.ach.implementation.errors.data.mapper

import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.ErrorMapper
import io.primer.android.stripe.ach.implementation.errors.domain.model.StripeError.StripeInvalidPublishableKeyError
import io.primer.android.stripe.ach.implementation.errors.domain.model.StripeError.StripeSdkError
import io.primer.android.stripe.exceptions.StripePublishableKeyMismatchException
import io.primer.android.stripe.exceptions.StripeSdkException

internal class StripeErrorMapper : ErrorMapper {
    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is StripePublishableKeyMismatchException -> StripeInvalidPublishableKeyError
            is StripeSdkException -> StripeSdkError(throwable.message.orEmpty())
            else -> error("Unsupported mapping for $throwable in ${this.javaClass.canonicalName}")
        }
    }
}
