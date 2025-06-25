package io.primer.android.internal.domain.interactor

import io.primer.android.components.domain.core.models.card.PrimerCardMetadataState
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.internal.domain.repositories.RawDataManagerRepository
import io.primer.cardShared.CardNumberFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map

class CardNetworkInteractor : DISdkComponent {

    private val rawDataManagerRepository: RawDataManagerRepository by lazy { resolve() }

    private val _detectedCardNetwork = MutableStateFlow<CardNetwork.Type>(CardNetwork.Type.OTHER)
    val detectedCardNetwork: Flow<CardNetwork.Type> = _detectedCardNetwork.asStateFlow()

    private val _selectedNetwork = MutableStateFlow<CardNetwork.Type?>(null)
    val selectedNetwork: Flow<CardNetwork.Type?> = _selectedNetwork.asStateFlow()

    val availableNetworks: Flow<List<PrimerCardNetwork>> = rawDataManagerRepository.metadataState
        .filterIsInstance<PrimerCardMetadataState.Fetched>()
        .map { metadataState ->
            val metadata = metadataState.cardNumberEntryMetadata
            val selectableNetworks = metadata.selectableCardNetworks?.items
            val detectedNetwork = metadata.detectedCardNetworks.preferred
                ?: metadata.detectedCardNetworks.items.firstOrNull()

            selectableNetworks ?: listOfNotNull(detectedNetwork)
        }

    val preferredNetwork: Flow<CardNetwork.Type?> = rawDataManagerRepository.metadataState
        .filterIsInstance<PrimerCardMetadataState.Fetched>()
        .map { metadataState ->
            metadataState.cardNumberEntryMetadata.selectableCardNetworks?.preferred?.network
        }

    fun updateDetectedCardNetwork(cardNumber: String) {
        try {
            val formatter = CardNumberFormatter.fromString(cardNumber)
            val detectedNetwork = formatter.getCardType()
            _detectedCardNetwork.value = detectedNetwork
        } catch (e: Exception) {
            _detectedCardNetwork.value = CardNetwork.Type.OTHER
        }
    }

    fun selectNetwork(network: CardNetwork.Type) {
        _selectedNetwork.value = network
    }

    fun getCardNetwork(): CardNetwork.Type {
        return _selectedNetwork.value ?: _detectedCardNetwork.value
    }
}
