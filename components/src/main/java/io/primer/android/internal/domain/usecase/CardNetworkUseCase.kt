package io.primer.android.internal.domain.usecase

import io.primer.android.api.components.card.PrimerCardFormController
import io.primer.android.components.domain.core.models.card.PrimerCardMetadataState
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.internal.domain.repositories.RawDataManagerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal class CardNetworkUseCase(rawDataManagerRepository: RawDataManagerRepository) {

    private val _selected = MutableStateFlow<PrimerCardNetwork?>(null)

    val networkSelection: Flow<PrimerCardFormController.NetworkSelection> = rawDataManagerRepository.metadataState
        .filterIsInstance<PrimerCardMetadataState.Fetched>()
        .map { it.cardNumberEntryMetadata }
        .combine(_selected) { meta, selected ->
            val selectable = meta.selectableCardNetworks?.items
            val detected = meta.detectedCardNetworks

            val availableNetworks =
                selectable ?: detected.items.takeIf { it.size > 1 } ?: listOfNotNull(detected.preferred)

            val selectedNetwork =
                selected?.takeIf { it in availableNetworks } ?: availableNetworks.firstOrNull()

            PrimerCardFormController.NetworkSelection(
                selectedNetwork = selectedNetwork,
                availableNetworks = availableNetworks,
                isNetworkSelectable = selectable != null,
            )
        }
        .onEach { selection ->
            if (selection.availableNetworks.size <= 1) _selected.value = null
        }

    fun selectCardNetwork(network: PrimerCardNetwork) {
        _selected.value = network
    }
}
