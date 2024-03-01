package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.error

import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.domain.error.models.KlarnaError
import io.primer.android.domain.error.models.PrimerError

internal class KlarnaErrorMapper : DefaultErrorMapper() {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is KlarnaUserUnapprovedException -> KlarnaError.UserUnapprovedError
            is KlarnaSdkErrorException -> KlarnaError.KlarnaSdkError(throwable.message)
            else -> return super.getPrimerError(throwable)
        }
    }
}
