package io.primer.android.clientToken.core.errors.data.mapper

import io.primer.android.clientToken.core.errors.data.exception.ExpiredClientTokenException
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import io.primer.android.clientToken.core.errors.domain.models.ClientTokenError
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.ErrorMapper

internal class ClientTokenErrorMapper : ErrorMapper {
    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is InvalidClientTokenException -> ClientTokenError.InvalidClientTokenError(description = throwable.message)
            is ExpiredClientTokenException -> ClientTokenError.ExpiredClientTokenError(description = throwable.message)
            else -> error("Unsupported mapping for $throwable in ${this.javaClass.canonicalName}")
        }
    }
}
