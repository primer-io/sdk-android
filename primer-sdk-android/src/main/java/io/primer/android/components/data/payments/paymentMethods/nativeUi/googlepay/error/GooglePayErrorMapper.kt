package io.primer.android.components.data.payments.paymentMethods.nativeUi.googlepay.error

import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.domain.error.models.GooglePayError
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.error.models.ShippingAddressUnserviceableError
import io.primer.android.domain.exception.GooglePayException
import io.primer.android.domain.exception.ShippingAddressUnserviceableException

internal class GooglePayErrorMapper : DefaultErrorMapper() {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is GooglePayException -> GooglePayError.GooglePayInternalError(throwable.status)
            is ShippingAddressUnserviceableException -> ShippingAddressUnserviceableError(throwable.shippingMethod)
            else -> return super.getPrimerError(throwable)
        }
    }
}
