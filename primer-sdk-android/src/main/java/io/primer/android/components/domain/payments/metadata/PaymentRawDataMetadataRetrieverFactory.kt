package io.primer.android.components.domain.payments.metadata

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.bancontact.PrimerBancontactCardData
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.payments.metadata.card.BancontactCardDataMetadataRetriever
import io.primer.android.components.domain.payments.metadata.card.CardDataMetadataRetriever
import io.primer.android.components.domain.payments.metadata.empty.EmptyMetadataRetriever

internal class PaymentRawDataMetadataRetrieverFactory {

    fun getMetadataRetriever(rawData: PrimerRawData):
        PaymentRawDataMetadataRetriever<PrimerRawData> {
        return when (rawData) {
            is PrimerCardData -> CardDataMetadataRetriever()
                as PaymentRawDataMetadataRetriever<PrimerRawData>
            is PrimerBancontactCardData -> BancontactCardDataMetadataRetriever()
                as PaymentRawDataMetadataRetriever<PrimerRawData>
            else -> EmptyMetadataRetriever()
        }
    }
}
