package io.primer.android.components.domain.payments.metadata.card

import io.primer.android.components.domain.core.models.bancontact.PrimerBancontactCardMetadata
import io.primer.android.components.domain.core.models.bancontact.PrimerBancontactCardData
import io.primer.android.components.domain.payments.metadata.PaymentRawDataMetadataRetriever
import io.primer.android.ui.CardNumberFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class BancontactCardDataMetadataRetriever :
    PaymentRawDataMetadataRetriever<PrimerBancontactCardData> {

    override fun retrieveMetadata(
        inputData: PrimerBancontactCardData
    ): Flow<PrimerBancontactCardMetadata> {
        return flow {
            emit(
                PrimerBancontactCardMetadata(
                    CardNumberFormatter.fromString(
                        inputData.cardNumber,
                        replaceInvalid = false
                    ).getCardType()
                )
            )
        }
    }
}
