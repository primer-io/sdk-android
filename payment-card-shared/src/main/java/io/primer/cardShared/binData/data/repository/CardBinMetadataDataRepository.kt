package io.primer.cardShared.binData.data.repository

import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.cardShared.binData.data.datasource.InMemoryCardBinMetadataDataSource
import io.primer.cardShared.binData.data.datasource.RemoteCardBinMetadataDataSource
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.components.domain.core.models.card.ValidationSource
import io.primer.cardShared.binData.domain.CardBinMetadata
import io.primer.cardShared.binData.domain.CardBinMetadataRepository

class CardBinMetadataDataRepository(
    private val localConfigurationDataSource: CacheConfigurationDataSource,
    private val remoteCardBinMetadataDataSource: RemoteCardBinMetadataDataSource,
    private val inMemoryCardBinMetadataDataSource: InMemoryCardBinMetadataDataSource
) : CardBinMetadataRepository {

    override suspend fun getBinMetadata(
        bin: String,
        source: ValidationSource
    ) = runSuspendCatching {
        when (source) {
            ValidationSource.REMOTE -> getRemoteBinData(bin)
            ValidationSource.LOCAL_FALLBACK -> getLocalBinData(bin)
            ValidationSource.LOCAL -> getLocalBinData(bin)
        }
    }

    private suspend fun getRemoteBinData(bin: String): List<CardBinMetadata> {
        val cardNetworkDataResponses =
            inMemoryCardBinMetadataDataSource.get()[bin].takeIf { cardNetworkDataResponses ->
                cardNetworkDataResponses.isNullOrEmpty().not()
            } ?: remoteCardBinMetadataDataSource.execute(
                BaseRemoteHostRequest(
                    localConfigurationDataSource.get().binDataUrl,
                    bin
                )
            ).also { cardNetworkDataResponses ->
                inMemoryCardBinMetadataDataSource.update(bin to cardNetworkDataResponses)
            }
        return cardNetworkDataResponses
            .map { metadata ->
                val network = metadata.value
                CardBinMetadata(
                    metadata.displayName,
                    network
                )
            }
    }

    private fun getLocalBinData(bin: String) = CardNetwork.lookupAll(bin)
        .map { descriptor ->
            CardBinMetadata(
                descriptor.type.displayName,
                descriptor.type
            )
        }
}
