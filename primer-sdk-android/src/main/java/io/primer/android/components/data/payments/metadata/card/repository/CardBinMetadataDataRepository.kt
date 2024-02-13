package io.primer.android.components.data.payments.metadata.card.repository

import io.primer.android.components.data.payments.metadata.card.datasource.InMemoryCardBinMetadataDataSource
import io.primer.android.components.data.payments.metadata.card.datasource.RemoteCardBinMetadataDataSource
import io.primer.android.components.domain.core.models.card.ValidationSource
import io.primer.android.components.domain.payments.metadata.card.model.CardBinMetadata
import io.primer.android.components.domain.payments.metadata.card.repository.CardBinMetadataRepository
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.extensions.runSuspendCatching
import io.primer.android.ui.CardNetwork

internal class CardBinMetadataDataRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource,
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
                BaseRemoteRequest(
                    localConfigurationDataSource.getConfiguration(),
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
                descriptor.type.getCardBrand().displayName,
                descriptor.type
            )
        }
}
