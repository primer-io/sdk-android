package io.primer.android.googlepay.implementation.errors.data.mapper

import io.primer.android.errors.domain.ErrorMapper
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.googlepay.implementation.errors.domain.model.ShippingAddressUnserviceableError
import io.primer.android.googlepay.implementation.errors.domain.exception.GooglePayException
import io.primer.android.googlepay.implementation.errors.domain.exception.ShippingAddressUnserviceableException
import io.primer.android.googlepay.implementation.errors.domain.model.GooglePayError

internal class GooglePayErrorMapper : ErrorMapper {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is GooglePayException -> GooglePayError.GooglePayInternalError(throwable.status)
            is ShippingAddressUnserviceableException -> ShippingAddressUnserviceableError(throwable.shippingMethod)
            else -> throw IllegalArgumentException("Unsupported mapping for $throwable")
        }
    }
}
