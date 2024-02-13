package io.primer.android.components.domain.payments.metadata.card

import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.core.models.card.PrimerCardMetadata
import io.primer.android.components.domain.payments.metadata.PaymentRawDataMetadataRetriever
import io.primer.android.ui.CardNumberFormatter

internal class CardDataMetadataRetriever :
    PaymentRawDataMetadataRetriever<PrimerCardData> {

    override suspend fun retrieveMetadata(
        inputData: PrimerCardData
    ) = PrimerCardMetadata(
        CardNumberFormatter.fromString(
            inputData.cardNumber,
            replaceInvalid = false
        ).getCardType()
    )
}
