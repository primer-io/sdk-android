package io.primer.android.paymentmethods.manager.composable

import io.primer.android.components.domain.error.PrimerInputValidationError
import kotlinx.coroutines.flow.Flow

/**
 * An interface representing a headless component that can validate data and emit validation status
 * and results as a [Flow] of [PrimerInputValidationError] lists.
 */
interface PrimerHeadlessInputValidatable<T : PrimerCollectableData> {
    /**
     * Get a [Flow] of [PrimerInputValidationError] objects representing validation status
     * emitted by this component.
     * Subscribers can observe and respond to validation status using this Flow.
     *
     * @return A Flow of [PrimerInputValidationError] object representing validation status
     * emitted by this component.
     */
    val componentInputValidations: Flow<List<PrimerInputValidationError>>
}
