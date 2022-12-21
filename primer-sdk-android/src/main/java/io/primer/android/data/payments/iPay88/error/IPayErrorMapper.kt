package io.primer.android.data.payments.iPay88.error

import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.domain.error.models.IPay88Error
import io.primer.android.domain.error.models.PrimerError
import io.primer.ipay88.api.exceptions.IPayConnectionErrorException
import io.primer.ipay88.api.exceptions.IPayPaymentFailedException

internal class IPayErrorMapper : DefaultErrorMapper() {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is IPayPaymentFailedException -> IPay88Error.IPaySdkPaymentFailedError(
                throwable.transactionId,
                throwable.refNo,
                throwable.errorDescription
            )
            is IPayConnectionErrorException -> IPay88Error.IPaySdkConnectionError
            else -> return super.getPrimerError(throwable)
        }
    }
}
