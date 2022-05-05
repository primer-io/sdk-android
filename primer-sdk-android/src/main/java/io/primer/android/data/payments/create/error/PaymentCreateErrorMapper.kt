package io.primer.android.data.payments.create.error

import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.http.exception.HttpException
import io.primer.android.domain.error.models.HttpError
import io.primer.android.domain.error.models.PaymentError
import io.primer.android.domain.error.models.PrimerError

internal class PaymentCreateErrorMapper : DefaultErrorMapper() {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        if (throwable is HttpException && throwable.isClientError()) {
            return HttpError.HttpClientError(
                throwable.errorCode,
                throwable.error.diagnosticsId,
                throwable.error.description,
                PaymentError.PaymentCreateFailedError(
                    throwable.error.description,
                    throwable.error.diagnosticsId
                )
            )
        }
        return super.getPrimerError(throwable)
    }
}
