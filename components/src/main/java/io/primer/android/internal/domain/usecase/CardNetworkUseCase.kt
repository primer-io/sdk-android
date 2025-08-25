package io.primer.android.internal.domain.usecase

import io.primer.android.components.domain.core.models.card.PrimerCardMetadataState
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.internal.domain.repositories.RawDataManagerRepository
import io.primer.cardShared.CardNumberFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map

internal class CardNetworkUseCase(
    private val rawDataManagerRepository: RawDataManagerRepository
) {

    private val _detectedCardNetwork = MutableStateFlow(CardNetwork.Type.OTHER)
    private val _selectedCardNetwork = MutableStateFlow<CardNetwork.Type?>(null)

    val currentCardNetwork: Flow<CardNetwork.Type> = combine(
        _detectedCardNetwork,
        _selectedCardNetwork,
    ) { detected, selected -> selected ?: detected }

    val availableNetworks: Flow<List<PrimerCardNetwork>> =
        rawDataManagerRepository.metadataState
            .filterIsInstance<PrimerCardMetadataState.Fetched>()
            .map { metadataState ->
                val metadata = metadataState.cardNumberEntryMetadata
                val selectableNetworks = metadata.selectableCardNetworks?.items
                val detectedNetwork = metadata.detectedCardNetworks.preferred
                    ?: metadata.detectedCardNetworks.items.firstOrNull()

                val networks = selectableNetworks ?: listOfNotNull(detectedNetwork)

                if (_selectedCardNetwork.value !in networks.map { it.network }) {
                    _selectedCardNetwork.value = null
                }

                networks
            }

    fun detectCardNetwork(cardNumber: String) {
        val formatter = CardNumberFormatter.fromString(cardNumber)
        _detectedCardNetwork.value = formatter.getCardType()
    }

    fun clear() {
        _selectedCardNetwork.value = null
        _detectedCardNetwork.value = CardNetwork.Type.OTHER
    }

    fun selectCardNetwork(network: CardNetwork.Type) {
        _selectedCardNetwork.value = network
    }
}
