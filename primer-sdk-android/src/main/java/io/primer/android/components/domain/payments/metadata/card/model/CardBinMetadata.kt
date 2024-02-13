package io.primer.android.components.domain.payments.metadata.card.model

import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.ui.CardNetwork

internal const val MAX_BIN_LENGTH = 8

internal data class CardBinMetadata(
    val displayName: String,
    val network: CardNetwork.Type?
)

/**
 * A function that returns ordered list of [PrimerCardNetwork].
 * All the allowed [PrimerCardNetwork] are extracted and sorted by
 * their index in the [allowedCardNetworks] list.
 * All unallowed [PrimerCardNetwork] are appended after, without the change in the sorting.
 */
internal fun List<CardBinMetadata>.toSortedPrimerCardNetworks(
    allowedCardNetworks: List<CardNetwork.Type>
) = this.asSequence().filter { cardBinMetadata -> cardBinMetadata.network != null }
    .filter { cardBinMetadata -> allowedCardNetworks.contains(cardBinMetadata.network) }
    .sortedBy { primerCardNetwork ->
        allowedCardNetworks.indexOf(primerCardNetwork.network)
    }.plus(
        this.filter { cardBinMetadata ->
            allowedCardNetworks.contains(cardBinMetadata.network).not()
        }
    )
    .map { metadata ->
        PrimerCardNetwork(
            requireNotNull(metadata.network),
            metadata.displayName,
            allowedCardNetworks.contains(metadata.network)
        )
    }.toList()
