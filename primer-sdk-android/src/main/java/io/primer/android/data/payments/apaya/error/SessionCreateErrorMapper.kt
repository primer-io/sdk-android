package io.primer.android.data.payments.apaya.error

import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.data.payments.exception.SessionCreateException
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.error.models.SessionCreateError

internal class SessionCreateErrorMapper : DefaultErrorMapper() {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        if (throwable is SessionCreateException) {
            return SessionCreateError(throwable.paymentMethodType, throwable.diagnosticsId)
        }
        return super.getPrimerError(throwable)
    }
}
