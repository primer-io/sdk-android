package io.primer.android.bancontact.implementation.metadata.domain

import io.primer.android.bancontact.PrimerBancontactCardData
import io.primer.android.bancontact.implementation.metadata.domain.model.PrimerBancontactCardMetadata
import io.primer.cardShared.CardNumberFormatter

internal class BancontactCardDataMetadataRetriever {

    suspend fun retrieveMetadata(
        inputData: PrimerBancontactCardData
    ) = PrimerBancontactCardMetadata(
        CardNumberFormatter.fromString(
            inputData.cardNumber,
            replaceInvalid = false
        ).getCardType()
    )
}
