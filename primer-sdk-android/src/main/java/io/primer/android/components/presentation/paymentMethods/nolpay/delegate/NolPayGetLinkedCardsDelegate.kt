package io.primer.android.components.presentation.paymentMethods.nolpay.delegate

import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.components.domain.payments.metadata.phone.PhoneMetadataInteractor
import io.primer.android.components.domain.payments.metadata.phone.model.PhoneMetadataParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkedCardsInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPaySdkInitInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayGetLinkedCardsParams
import io.primer.android.extensions.flatMap
import io.primer.nolpay.api.models.PrimerNolPaymentCard

internal class NolPayGetLinkedCardsDelegate(
    private val getLinkedCardsInteractor: NolPayGetLinkedCardsInteractor,
    private val phoneMetadataInteractor: PhoneMetadataInteractor,
    analyticsInteractor: AnalyticsInteractor,
    sdkInitInteractor: NolPaySdkInitInteractor
) : BaseNolPayDelegate(sdkInitInteractor, analyticsInteractor) {

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
