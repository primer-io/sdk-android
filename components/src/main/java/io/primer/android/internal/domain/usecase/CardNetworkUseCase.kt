package io.primer.android.internal.domain.usecase

import io.primer.android.components.domain.core.models.card.PrimerCardMetadataState
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.internal.domain.repositories.RawDataManagerRepository
import io.primer.cardShared.CardNumberFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map

internal class CardNetworkUseCase : DISdkComponent {

    private val rawDataManagerRepository: RawDataManagerRepository by lazy { resolve() }

    private val _detectedCardNetwork = MutableStateFlow(CardNetwork.Type.OTHER)
    private val _selectedCardNetwork = MutableStateFlow<CardNetwork.Type?>(null)

    val currentCardNetwork: Flow<CardNetwork.Type> = combine(
        _detectedCardNetwork,
        _selectedCardNetwork
    ) { detected, selected -> selected ?: detected }

    val availableNetworks: Flow<List<PrimerCardNetwork>> = rawDataManagerRepository.metadataState
        .filterIsInstance<PrimerCardMetadataState.Fetched>()
        .map { metadataState ->
            val metadata = metadataState.cardNumberEntryMetadata
            val selectableNetworks = metadata.selectableCardNetworks?.items
            val detectedNetwork = metadata.detectedCardNetworks.preferred
                ?: metadata.detectedCardNetworks.items.firstOrNull()

            selectableNetworks ?: listOfNotNull(detectedNetwork)
        }

    fun detectCardNetwork(cardNumber: String) {
        try {
            val formatter = CardNumberFormatter.fromString(cardNumber)
            _detectedCardNetwork.value = formatter.getCardType()
        } catch (_: Exception) {
            _detectedCardNetwork.value = CardNetwork.Type.OTHER
        }
    }

    fun selectCardNetwork(network: CardNetwork.Type) {
        _selectedCardNetwork.value = network
    }
}
