package io.primer.android.components.domain.payments.metadata.card

import io.primer.android.components.domain.core.models.bancontact.PrimerBancontactCardMetadata
import io.primer.android.components.domain.core.models.bancontact.PrimerBancontactCardData
import io.primer.android.components.domain.payments.metadata.PaymentRawDataMetadataRetriever
import io.primer.android.ui.CardNumberFormatter

internal class BancontactCardDataMetadataRetriever :
    PaymentRawDataMetadataRetriever<PrimerBancontactCardData> {

    override suspend fun retrieveMetadata(
        inputData: PrimerBancontactCardData
    ) = PrimerBancontactCardMetadata(
        CardNumberFormatter.fromString(
            inputData.cardNumber,
            replaceInvalid = false
        ).getCardType()
    )
}
