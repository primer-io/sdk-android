// package structure is kept in order to maintain backward compatibility
package io.primer.android.components.domain.core.models.card

import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadataState

/**
 * A sealed class that encapsulates various states related to card metadata retrieval and processing.
 */
sealed class PrimerCardMetadataState : PrimerPaymentMethodMetadataState {

    /**
     * Represents the state when fetching card metadata, including the associated [PrimerCardNumberEntryState].
     *
     * @property cardNumberEntryState The state of card number entry triggered the metadata fetch.
     */
    data class Fetching(val cardNumberEntryState: PrimerCardNumberEntryState) :
        PrimerCardMetadataState()

    /**
     * Represents the state when card metadata has been successfully fetched, including metadata and associated state.
     *
     * @property cardNumberEntryMetadata The metadata associated with the [PrimerCardNumberEntryState].
     * @property cardNumberEntryState The state of the card number entry that the metadata was fetched for.
     */
    data class Fetched(
        val cardNumberEntryMetadata: PrimerCardNumberEntryMetadata,
        val cardNumberEntryState: PrimerCardNumberEntryState
    ) : PrimerCardMetadataState()
}
