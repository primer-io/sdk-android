package io.primer.android.components.data.payments.paymentMethods.nolpay.error

import com.snowballtech.transit.rta.TransitException
import io.primer.android.domain.error.ErrorMapper
import io.primer.android.domain.error.models.GeneralError
import io.primer.android.domain.error.models.NolPayError
import io.primer.android.domain.error.models.PrimerError

class NolPayErrorMapper : ErrorMapper {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is TransitException -> NolPayError(
                throwable.code,
                throwable.message
            )

            else -> GeneralError.UnknownError(throwable.message.orEmpty())
        }
    }
}
