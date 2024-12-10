package io.primer.android.paymentmethods.manager.composable

import io.primer.android.domain.error.models.PrimerError
import kotlinx.coroutines.flow.Flow

/**
 * An interface representing a headless component that can emit errors as a Flow of [PrimerError] objects.
 */
interface PrimerHeadlessErrorable {
    /**
     * Get a [Flow] of [PrimerError] objects that represent errors emitted by this component.
     * Subscribers can observe and handle errors using this Flow.
     *
     * @return A Flow of [PrimerError] objects representing errors emitted by this component.
     */
    val componentError: Flow<PrimerError>
}
