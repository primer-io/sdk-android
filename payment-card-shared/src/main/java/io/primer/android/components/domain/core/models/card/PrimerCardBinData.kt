package io.primer.android.components.domain.core.models.card

import io.primer.android.configuration.data.model.CardNetwork

data class PrimerCardBinData(
    val network: CardNetwork.Type,
    val displayName: String,
    val issuerCountryCode: String,
    val issuerName: String?,
    val accountFundingType: String,
    val prepaidReloadableIndicator: String,
    val productUsageType: String,
    val productCode: String,
    val productName: String,
    val issuerCurrencyCode: String?,
    val regionalRestriction: String,
    val accountNumberType: String,
)

data class PrimerCardBinDataMetadata(
    val preferred: PrimerCardBinData?,
    val alternatives: List<PrimerCardBinData>,
    val firstDigits: String,
)
