package io.primer.android.components.domain.payments.metadata.card

import io.primer.android.components.domain.core.models.card.PrimerCardNumberEntryMetadata

internal class CardMetadataCacheHelper {

    private val metadataCache: HashMap<String, PrimerCardNumberEntryMetadata> = hashMapOf()

    fun getCardNetworksMetadata(bin: String) = metadataCache[bin]

    fun saveCardNetworksMetadata(bin: String, cardNetworksMetadata: PrimerCardNumberEntryMetadata) {
        metadataCache[bin] = cardNetworksMetadata
    }
}
