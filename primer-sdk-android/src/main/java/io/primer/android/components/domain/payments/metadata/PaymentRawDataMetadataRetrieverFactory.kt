package io.primer.android.components.domain.payments.metadata

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.card.PrimerRawCardData

internal class PaymentRawDataMetadataRetrieverFactory {

    fun getMetadataRetriever(rawData: PrimerRawData):
        PaymentRawDataMetadataRetriever<PrimerRawData> {
        return when (rawData) {
            is PrimerRawCardData -> CardDataMetadataRetriever()
                as PaymentRawDataMetadataRetriever<PrimerRawData>
            else -> throw IllegalArgumentException(
                "Unsupported data validation for ${rawData::class}."
            )
        }
    }
}
