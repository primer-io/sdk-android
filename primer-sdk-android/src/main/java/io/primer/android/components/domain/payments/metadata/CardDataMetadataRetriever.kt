package io.primer.android.components.domain.payments.metadata

import io.primer.android.components.domain.core.models.card.PrimerRawCardData
import io.primer.android.components.domain.core.models.card.PrimerCardMetadata
import io.primer.android.ui.CardNumberFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class CardDataMetadataRetriever : PaymentRawDataMetadataRetriever<PrimerRawCardData> {

    override fun retrieveMetadata(
        inputData: PrimerRawCardData
    ): Flow<PrimerCardMetadata> {
        return flow {
            emit(
                PrimerCardMetadata(
                    CardNumberFormatter.fromString(
                        inputData.cardNumber,
                        replaceInvalid = false
                    ).getCardType()
                )
            )
        }
    }
}
