package io.primer.android.components.domain.core.models.card

import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadata
import io.primer.android.ui.CardNetwork

data class PrimerCardMetadata(val cardNetwork: CardNetwork.Type) :
    PrimerPaymentMethodMetadata

/**
 * Represents metadata for a list of [PrimerCardNetwork], including a preferred network.
 *
 * @property items List of [PrimerCardNetwork] objects.
 * @property preferred Preferred [PrimerCardNetwork], can be null if no preference is specified,
 * or if none of the networks are allowed.
 */
data class PrimerCardNetworksMetadata(
    val items: List<PrimerCardNetwork>,
    val preferred: PrimerCardNetwork?
)

/**
 * Represents metadata for PrimerCardNumberEntry, including selectable (co-badged) and detected card networks.
 *
 * @property selectableCardNetworks Metadata for selectable card networks for a given card number,
 * will be null in case card network is not co-badged .
 * @property detectedCardNetworks Metadata for all detected card networks for a given card number.
 * @property source The source of validation, indicating whether it's from remote, local fallback, or local.
 */
data class PrimerCardNumberEntryMetadata(
    val selectableCardNetworks: PrimerCardNetworksMetadata?,
    val detectedCardNetworks: PrimerCardNetworksMetadata,
    val source: ValidationSource
) : PrimerPaymentMethodMetadata

/**
 * Enum class representing the source of validation for card metadata.
 *
 * @property REMOTE Validation source from a remote server.
 * @property LOCAL_FALLBACK Validation source from a local fallback mechanism in case the remote validation failed.
 * @property LOCAL Validation source from a local validation process.
 */
enum class ValidationSource {
    REMOTE,
    LOCAL_FALLBACK,
    LOCAL
}
