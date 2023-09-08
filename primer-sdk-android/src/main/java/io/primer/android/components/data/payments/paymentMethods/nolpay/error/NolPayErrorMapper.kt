package io.primer.android.components.data.payments.paymentMethods.nolpay.error

import io.primer.android.domain.error.ErrorMapper
import io.primer.android.domain.error.models.GeneralError
import io.primer.android.domain.error.models.NolPayError
import io.primer.android.domain.error.models.PrimerError
import io.primer.nolpay.exceptions.NolPaySdkException

class NolPayErrorMapper : ErrorMapper {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is NolPaySdkException -> NolPayError(
                throwable.errorCode,
                throwable.message
            )

            else -> GeneralError.UnknownError(throwable.message.orEmpty())
        }
    }
}
