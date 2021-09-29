package io.primer.android.extensions

import io.primer.android.data.exception.HttpException
import io.primer.android.events.CheckoutEvent
import io.primer.android.model.dto.APIError
import java.io.IOException

internal fun Throwable.toCheckoutErrorEvent() =
    when (this) {
        is HttpException -> CheckoutEvent.ApiError(error)
        is IOException -> CheckoutEvent.ApiError(APIError.create(this))
        else -> CheckoutEvent.ApiError(APIError.createDefault())
    }

internal fun Throwable.toTokenizationErrorEvent(message: String? = null) =
    if (message.isNullOrBlank().not()) {
        CheckoutEvent.TokenizationError(APIError.createDefaultWithMessage(message.orEmpty()))
    } else when (this) {
        is HttpException -> CheckoutEvent.TokenizationError(error)
        is IOException -> CheckoutEvent.TokenizationError(APIError.create(this))
        else -> CheckoutEvent.TokenizationError(APIError.createDefault())
    }

internal fun Throwable.toResumeErrorEvent(message: String? = null) =
    if (message.isNullOrBlank().not()) {
        CheckoutEvent.ResumeError(APIError.createDefaultWithMessage(message.orEmpty()))
    } else when (this) {
        is HttpException -> CheckoutEvent.ResumeError(error)
        is IOException -> CheckoutEvent.ResumeError(APIError.create(this))
        else -> CheckoutEvent.ResumeError(APIError.createDefault())
    }
