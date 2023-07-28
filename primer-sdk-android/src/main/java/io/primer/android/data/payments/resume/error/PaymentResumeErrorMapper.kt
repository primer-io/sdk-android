package io.primer.android.data.payments.resume.error

import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.domain.error.models.HttpError
import io.primer.android.domain.error.models.PaymentError
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.http.exception.HttpException

internal class PaymentResumeErrorMapper : DefaultErrorMapper() {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is HttpException ->
                when (throwable.isClientError()) {
                    true -> HttpError.HttpClientError(
                        throwable.errorCode,
                        throwable.error.diagnosticsId,
                        throwable.error.description,
                        PaymentError.PaymentResumeFailedError(
                            throwable.error.description,
                            throwable.error.diagnosticsId
                        )
                    )
                    else -> super.getPrimerError(throwable)
                }
            else -> return super.getPrimerError(throwable)
        }
    }
}
