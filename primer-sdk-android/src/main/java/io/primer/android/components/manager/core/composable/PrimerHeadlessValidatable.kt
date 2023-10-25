package io.primer.android.components.manager.core.composable

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.domain.error.models.PrimerError
import kotlinx.coroutines.flow.Flow

/**
 * A sealed interface representing the status of data validation.
 * Instances of this interface can be used to indicate the result of validation operations.
 */
sealed interface PrimerValidationStatus<T : PrimerCollectableData> {

    /**
     * Indicates that data is currently in the process of being validated.
     *
     * @param collectableData The data being validated.
     */
    data class Validating<T : PrimerCollectableData>(val collectableData: PrimerCollectableData) :
        PrimerValidationStatus<T>

    /**
     * Indicates that the data has successfully been validated.
     *
     * @param collectableData The successfully validated data.
     */
    data class Valid<T : PrimerCollectableData>(
        val collectableData: PrimerCollectableData
    ) : PrimerValidationStatus<T>

    /**
     * Indicates that the data has been considered invalid after validation.
     *
     * @param errors A list of [PrimerValidationError] explaining why the data is considered invalid.
     * @param collectableData The data that failed validation.
     */
    data class Invalid<T : PrimerCollectableData>(
        val errors: List<PrimerValidationError>,
        val collectableData: PrimerCollectableData
    ) : PrimerValidationStatus<T>

    /**
     * Represents the status when an error occurred during the validation process.
     *
     * @param error The specific [PrimerError] that occurred during validation.
     * @param collectableData The data for which the error occurred.
     */
    data class Error<T : PrimerCollectableData>(
        val error: PrimerError,
        val collectableData: PrimerCollectableData
    ) : PrimerValidationStatus<T>
}

/**
 * An interface representing a headless component that can validate data and emit validation status
 * and results as a [Flow] of [PrimerValidationStatus] lists.
 */
interface PrimerHeadlessValidatable<T : PrimerCollectableData> {
    /**
     * Get a [Flow] of [PrimerValidationStatus] objects representing validation status
     * emitted by this component.
     * Subscribers can observe and respond to validation status using this Flow.
     *
     * @return A Flow of [PrimerValidationStatus] object representing validation status
     * emitted by this component.
     */
    val componentValidationStatus: Flow<PrimerValidationStatus<T>>
}
