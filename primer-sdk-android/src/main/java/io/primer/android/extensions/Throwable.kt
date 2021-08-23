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
