package io.primer.android.domain.error

import io.primer.android.domain.error.models.PrimerError

internal interface ErrorMapper {

    fun getPrimerError(throwable: Throwable): PrimerError
}
