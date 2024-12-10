package io.primer.cardShared.binData.domain

import io.primer.android.components.domain.core.models.card.PrimerCardNumberEntryMetadata

class CardMetadataCacheHelper {

    private val metadataCache: HashMap<String, PrimerCardNumberEntryMetadata> = hashMapOf()

    fun getCardNetworksMetadata(bin: String) = metadataCache[bin]

    fun saveCardNetworksMetadata(bin: String, cardNetworksMetadata: PrimerCardNumberEntryMetadata) {
        metadataCache[bin] = cardNetworksMetadata
    }
}
