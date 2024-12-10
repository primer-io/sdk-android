package io.primer.android.errors.data.mapper

import io.primer.android.errors.domain.ErrorMapper
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.models.PrimerUnknownError

internal class DefaultErrorMapperRegistry : ErrorMapperRegistry {

    private val errorMappers = mutableListOf<ErrorMapper>()

    init {
        // register default error mapper
        errorMappers.add(DefaultErrorMapper())
    }
    override fun register(errorMapper: ErrorMapper) {
        errorMappers.add(errorMapper)
    }

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return errorMappers.firstNotNullOfOrNull {
            runCatching {
                it.getPrimerError(throwable)
            }.getOrNull()
        } ?: PrimerUnknownError(throwable.message.orEmpty())
    }
}
