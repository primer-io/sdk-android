package io.primer.android.clientSessionActions.data.errors.mapper

import io.primer.android.clientSessionActions.domain.errors.model.ActionUpdateFailedError
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.data.exception.SessionUpdateException
import io.primer.android.errors.domain.ErrorMapper

internal class ActionUpdateErrorMapper : ErrorMapper {
    override fun getPrimerError(throwable: Throwable): PrimerError {
        if (throwable is SessionUpdateException) {
            return ActionUpdateFailedError(
                throwable.description,
                throwable.diagnosticsId,
            )
        }
        throw IllegalArgumentException("Unsupported mapping for $throwable")
    }
}
