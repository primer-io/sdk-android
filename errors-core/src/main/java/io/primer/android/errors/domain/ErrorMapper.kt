package io.primer.android.errors.domain

import io.primer.android.domain.error.models.PrimerError

fun interface ErrorMapper {

    fun getPrimerError(throwable: Throwable): PrimerError
}
