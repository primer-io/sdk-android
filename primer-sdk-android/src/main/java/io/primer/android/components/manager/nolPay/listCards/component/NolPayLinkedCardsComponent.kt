package io.primer.android.components.manager.nolPay.listCards.component

import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkedCardsInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayGetLinkedCardsParams
import io.primer.android.components.manager.core.composable.PrimerHeadlessComponent
import io.primer.android.components.manager.nolPay.analytics.NolPayAnalyticsConstants
import io.primer.android.di.DIAppComponent
import io.primer.nolpay.models.PrimerNolPaymentCard
import kotlinx.coroutines.flow.collect
import org.koin.core.component.get

class NolPayLinkedCardsComponent internal constructor(
    private val nolPayGetLinkedCardsInteractor: NolPayGetLinkedCardsInteractor,
    private val analyticsInteractor: AnalyticsInteractor
) : PrimerHeadlessComponent {

    suspend fun getLinkedCards(
        mobileNumber: String,
        phoneCountryDiallingCode: String
    ): Result<List<PrimerNolPaymentCard>> {
        logSdkFunctionCalls(NolPayAnalyticsConstants.LINKED_CARDS_GET_CARDS_METHOD)
        return nolPayGetLinkedCardsInteractor(
            NolPayGetLinkedCardsParams(
                mobileNumber,
                phoneCountryDiallingCode
            )
        )
    }

    private suspend fun logSdkFunctionCalls(
        methodName: String,
        context: Map<String, String> = hashMapOf()
    ) = analyticsInteractor(
        SdkFunctionParams(
            methodName,
            mapOf(
                "category" to PrimerPaymentMethodManagerCategory.NOL_PAY.name
            ).plus(context)
        )
    ).collect()

    internal companion object : DIAppComponent {

        fun getInstance() = NolPayLinkedCardsComponent(get(), get())
    }
}
