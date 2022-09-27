package io.primer.android.components.domain.payments.metadata

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.bancontact.PrimerRawBancontactCardData
import io.primer.android.components.domain.core.models.card.PrimerRawCardData
import io.primer.android.components.domain.payments.metadata.card.BancontactCardDataMetadataRetriever
import io.primer.android.components.domain.payments.metadata.card.CardDataMetadataRetriever
import io.primer.android.components.domain.payments.metadata.empty.EmptyMetadataRetriever

internal class PaymentRawDataMetadataRetrieverFactory {

    fun getMetadataRetriever(rawData: PrimerRawData):
        PaymentRawDataMetadataRetriever<PrimerRawData> {
        return when (rawData) {
            is PrimerRawCardData -> CardDataMetadataRetriever()
                as PaymentRawDataMetadataRetriever<PrimerRawData>
            is PrimerRawBancontactCardData -> BancontactCardDataMetadataRetriever()
                as PaymentRawDataMetadataRetriever<PrimerRawData>
            else -> EmptyMetadataRetriever()
        }
    }
}
