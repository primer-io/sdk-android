package io.primer.sample.datamodels

import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.payments.create.model.Payment

data class CheckoutDataWithError(val payment: Payment? = null, val error: PrimerMappedError? = null)

data class PrimerMappedError(
    val errorId: String,
    val description: String,
    val diagnosticsId: String,
    val recoverySuggestion: String?
)

internal fun PrimerError.toMappedError() = PrimerMappedError(
    errorId,
    description,
    diagnosticsId,
    recoverySuggestion
)
