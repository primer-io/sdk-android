package io.primer.android.components.data.payments.paymentMethods.nativeUi.googlepay.error

import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.domain.error.models.GooglePayError
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.exception.GooglePayException

internal class GooglePayErrorMapper : DefaultErrorMapper() {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is GooglePayException -> GooglePayError.GooglePayInternalError(throwable.status)
            else -> return super.getPrimerError(throwable)
        }
    }
}
