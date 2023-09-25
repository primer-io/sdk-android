package io.primer.android.components.manager.core.composable

import io.primer.android.components.domain.error.PrimerValidationError
import kotlinx.coroutines.flow.Flow

/**
 * An interface representing a headless component that can validate data and emit validation results
 * as a [Flow] of [PrimerValidationError] lists.
 */
interface PrimerHeadlessValidatable {
    /**
     * Get a [Flow] of lists of [PrimerValidationError] objects representing validation results
     * emitted by this component.
     * Subscribers can observe and respond to validation results using this Flow.
     *
     * @return A Flow of lists of [PrimerValidationError] objects representing validation results
     * emitted by this component.
     */
    val validationErrors: Flow<List<PrimerValidationError>>
}
