package io.primer.android.errors.domain

import io.primer.android.domain.error.models.PrimerError

interface ErrorMapperRegistry {

    fun register(errorMapper: ErrorMapper)

    fun getPrimerError(throwable: Throwable): PrimerError
}
