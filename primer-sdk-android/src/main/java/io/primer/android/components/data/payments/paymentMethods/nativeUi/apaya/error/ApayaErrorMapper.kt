package io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.error

import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.domain.error.models.ApayaError
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.exception.ApayaException

internal class ApayaErrorMapper : DefaultErrorMapper() {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is ApayaException -> ApayaError.ApayaInternalError(throwable.status)
            else -> return super.getPrimerError(throwable)
        }
    }
}
