package io.primer.android.components.presentation.paymentMethods.nolpay.delegate

import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayAppSecretInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkedCardsInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayGetLinkedCardsParams
import io.primer.android.extensions.flatMap
import io.primer.nolpay.api.models.PrimerNolPaymentCard

internal class NolPayGetLinkedCardsDelegate(
    private val getLinkedCardsInteractor: NolPayGetLinkedCardsInteractor,
    analyticsInteractor: AnalyticsInteractor,
    appSecretInteractor: NolPayAppSecretInteractor,
    configurationInteractor: NolPayConfigurationInteractor
) : BaseNolPayDelegate(appSecretInteractor, configurationInteractor, analyticsInteractor) {

    suspend fun getLinkedCards(
        mobileNumber: String,
        phoneCountryDiallingCode: String
    ): Result<List<PrimerNolPaymentCard>> {
        return start().flatMap {
            getLinkedCardsInteractor(
                NolPayGetLinkedCardsParams(
                    mobileNumber,
                    phoneCountryDiallingCode
                )
            )
        }
    }
}
