package io.primer.cardShared.binData.data.repository

import io.primer.android.components.domain.core.models.card.ValidationSource
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.cardShared.binData.data.datasource.InMemoryCardBinMetadataDataSource
import io.primer.cardShared.binData.data.datasource.RemoteCardBinMetadataDataSource
import io.primer.cardShared.binData.domain.CardBinMetadata
import io.primer.cardShared.binData.domain.CardBinMetadataRepository
import io.primer.cardShared.binData.domain.CardBinMetadataResult

class CardBinMetadataDataRepository(
    private val localConfigurationDataSource: CacheConfigurationDataSource,
    private val remoteCardBinMetadataDataSource: RemoteCardBinMetadataDataSource,
    private val inMemoryCardBinMetadataDataSource: InMemoryCardBinMetadataDataSource,
) : CardBinMetadataRepository {
    override suspend fun getBinMetadata(
        bin: String,
        source: ValidationSource,
    ) = runSuspendCatching {
        when (source) {
            ValidationSource.REMOTE -> getRemoteBinData(bin)
            ValidationSource.LOCAL_FALLBACK -> getLocalBinData(bin)
            ValidationSource.LOCAL -> getLocalBinData(bin)
        }
    }

    private suspend fun getRemoteBinData(bin: String): CardBinMetadataResult {
        val binDataResponse =
            inMemoryCardBinMetadataDataSource.get()[bin]
                ?: remoteCardBinMetadataDataSource.execute(
                    BaseRemoteHostRequest(
                        localConfigurationDataSource.get().binDataUrl,
                        bin,
                    ),
                ).also { response ->
                    inMemoryCardBinMetadataDataSource.update(bin to response)
                }
        return CardBinMetadataResult(
            items = binDataResponse.binData.map { item ->
                CardBinMetadata(
                    displayName = item.displayName,
                    network = item.network,
                    issuerCountryCode = item.issuerCountryCode,
                    issuerName = item.issuerName,
                    accountFundingType = item.accountFundingType,
                    prepaidReloadableIndicator = item.prepaidReloadableIndicator,
                    productUsageType = item.productUsageType,
                    productCode = item.productCode,
                    productName = item.productName,
                    issuerCurrencyCode = item.issuerCurrencyCode,
                    regionalRestriction = item.regionalRestriction,
                    accountNumberType = item.accountNumberType,
                )
            },
            firstDigits = binDataResponse.firstDigits,
        )
    }

    private fun getLocalBinData(bin: String) =
        CardBinMetadataResult(
            items = CardNetwork.lookupAll(bin).map { descriptor ->
                CardBinMetadata(
                    descriptor.type.displayName,
                    descriptor.type,
                )
            },
            firstDigits = null,
        )
}
