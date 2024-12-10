package io.primer.cardShared.binData.domain

import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork

const val MAX_BIN_LENGTH = 8

data class CardBinMetadata(
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
            cardBinMetadata.network != null && allowedCardNetworks.contains(cardBinMetadata.network).not()
        }
    )
    .map { metadata ->
        PrimerCardNetwork(
            requireNotNull(metadata.network),
            metadata.displayName,
            allowedCardNetworks.contains(metadata.network)
        )
    }.toList()
