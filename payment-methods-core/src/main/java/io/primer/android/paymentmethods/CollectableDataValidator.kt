package io.primer.android.paymentmethods

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.paymentmethods.manager.composable.PrimerCollectableData

fun interface CollectableDataValidator<out T : PrimerCollectableData> {
    suspend fun validate(t: @UnsafeVariance T): Result<List<PrimerValidationError>>
}
