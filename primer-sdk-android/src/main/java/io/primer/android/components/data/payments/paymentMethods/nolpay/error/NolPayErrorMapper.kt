package io.primer.android.components.data.payments.paymentMethods.nolpay.error

import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.domain.error.models.NolPayError
import io.primer.android.domain.error.models.PrimerError
import io.primer.nolpay.api.exceptions.NolPaySdkException

internal class NolPayErrorMapper : DefaultErrorMapper() {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is NolPaySdkException -> NolPayError(
                throwable.errorCode,
                throwable.message
            )

            else -> super.getPrimerError(throwable)
        }
    }
}
