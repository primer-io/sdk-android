package io.primer.android.nolpay.implementation.listCards.presentation

import io.primer.nolpay.api.models.PrimerNolPaymentCard
import io.primer.android.nolpay.implementation.common.domain.NolPaySdkInitInteractor
import io.primer.android.core.extensions.flatMap
import io.primer.android.nolpay.implementation.common.presentation.BaseNolPayDelegate
import io.primer.android.nolpay.implementation.listCards.domain.NolPayGetLinkedCardsInteractor
import io.primer.android.nolpay.implementation.listCards.domain.model.NolPayGetLinkedCardsParams
import io.primer.android.phoneMetadata.domain.PhoneMetadataInteractor
import io.primer.android.phoneMetadata.domain.model.PhoneMetadataParams

internal class NolPayGetLinkedCardsDelegate(
    private val getLinkedCardsInteractor: NolPayGetLinkedCardsInteractor,
    private val phoneMetadataInteractor: PhoneMetadataInteractor,
    override val sdkInitInteractor: NolPaySdkInitInteractor
) : BaseNolPayDelegate {

    suspend fun getLinkedCards(
        mobileNumber: String
    ): Result<List<PrimerNolPaymentCard>> {
        return start().flatMap {
            phoneMetadataInteractor(PhoneMetadataParams(mobileNumber))
        }.flatMap { phoneMetadata ->
            getLinkedCardsInteractor(
                NolPayGetLinkedCardsParams(
                    phoneMetadata.nationalNumber,
                    phoneMetadata.countryCode
                )
            )
        }
    }
}
