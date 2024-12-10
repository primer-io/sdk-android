package io.primer.android.klarna.implementation.errors.data.mapper

import io.primer.android.errors.domain.ErrorMapper
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.klarna.implementation.errors.data.exception.KlarnaSdkErrorException
import io.primer.android.klarna.implementation.errors.data.exception.KlarnaUserUnapprovedException
import io.primer.android.klarna.implementation.errors.domain.model.KlarnaError

internal class KlarnaErrorMapper : ErrorMapper {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is KlarnaUserUnapprovedException -> KlarnaError.UserUnapprovedError
            is KlarnaSdkErrorException -> KlarnaError.KlarnaSdkError(throwable.message)
            else -> error("Unsupported mapping for $throwable in ${this.javaClass.canonicalName}")
        }
    }
}
