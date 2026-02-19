package io.primer.cardShared.binData.domain

import io.primer.android.components.domain.core.models.card.PrimerCardBinData
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.configuration.data.model.CardNetwork

const val MAX_BIN_LENGTH = 8

data class CardBinMetadata(
    val displayName: String,
    val network: CardNetwork.Type?,
    val issuerCountryCode: String? = null,
    val issuerName: String? = null,
    val accountFundingType: String? = null,
    val prepaidReloadableIndicator: String? = null,
    val productUsageType: String? = null,
    val productCode: String? = null,
    val productName: String? = null,
    val issuerCurrencyCode: String? = null,
    val regionalRestriction: String? = null,
    val accountNumberType: String? = null,
)

data class CardBinMetadataResult(
    val items: List<CardBinMetadata>,
    val firstDigits: String?,
)

internal fun List<CardBinMetadata>.sortedByAllowedNetworks(
    allowedCardNetworks: List<CardNetwork.Type>,
): List<CardBinMetadata> =
    this.filter { it.network != null && allowedCardNetworks.contains(it.network) }
        .sortedBy { allowedCardNetworks.indexOf(it.network) }

/**
 * A function that returns ordered list of [PrimerCardNetwork].
 * All the allowed [PrimerCardNetwork] are extracted and sorted by
 * their index in the [allowedCardNetworks] list.
 * All unallowed [PrimerCardNetwork] are appended after, without the change in the sorting.
 */
internal fun List<CardBinMetadata>.toSortedPrimerCardNetworks(allowedCardNetworks: List<CardNetwork.Type>) =
    sortedByAllowedNetworks(allowedCardNetworks)
        .map { metadata ->
            PrimerCardNetwork(
                requireNotNull(metadata.network),
                metadata.displayName,
                true,
            )
        }.plus(
            this.filter { cardBinMetadata ->
                cardBinMetadata.network != null && allowedCardNetworks.contains(cardBinMetadata.network).not()
            }.map { metadata ->
                PrimerCardNetwork(
                    requireNotNull(metadata.network),
                    metadata.displayName,
                    false,
                )
            },
        )

internal fun CardBinMetadata.toPrimerCardBinData() =
    PrimerCardBinData(
        network = requireNotNull(network),
        displayName = displayName,
        issuerCountryCode = issuerCountryCode.orEmpty(),
        issuerName = issuerName,
        accountFundingType = accountFundingType.orEmpty(),
        prepaidReloadableIndicator = prepaidReloadableIndicator.orEmpty(),
        productUsageType = productUsageType.orEmpty(),
        productCode = productCode.orEmpty(),
        productName = productName.orEmpty(),
        issuerCurrencyCode = issuerCurrencyCode,
        regionalRestriction = regionalRestriction.orEmpty(),
        accountNumberType = accountNumberType.orEmpty(),
    )
