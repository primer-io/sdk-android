package io.primer.cardShared.binData.domain

import io.primer.cardShared.CardNumberFormatter
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.core.models.card.PrimerCardMetadata

class CardDataMetadataRetriever {

    suspend fun retrieveMetadata(
        inputData: PrimerCardData
    ) = PrimerCardMetadata(
        CardNumberFormatter.fromString(
            inputData.cardNumber,
            replaceInvalid = false
        ).getCardType()
    )
}
