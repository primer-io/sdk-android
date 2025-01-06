package io.primer.android.nolpay.api.manager.listCards.component

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.nolpay.api.manager.analytics.NolPayAnalyticsConstants
import io.primer.android.nolpay.implementation.listCards.presentation.NolPayGetLinkedCardsDelegate
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessComponent
import io.primer.nolpay.api.models.PrimerNolPaymentCard

class NolPayLinkedCardsComponent internal constructor(
    private val linkedCardsDelegate: NolPayGetLinkedCardsDelegate,
    private val eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate,
) : PrimerHeadlessComponent {
    /**
     * Retrieves a list of linked Nol Pay cards associated with the specified mobile number and
     * phone country dialing code.
     *
     * @param mobileNumber The mobile number in E.164 format for which linked cards are to be retrieved.
     *
     * @return A [Result] containing either a list of linked [PrimerNolPaymentCard] objects on
     * success or an error on failure.
     * In case of error, as part of [Result] object, SDK will return:
     * [io.primer.android.components.domain.payments.metadata.phone.exception.PhoneValidationException]
     * if an error occurs while fetching validating [mobileNumber].
     * [io.primer.nolpay.api.exceptions.NolPaySdkException] if an error occurs while fetching the linked cards.
     */
    suspend fun getLinkedCards(mobileNumber: String): Result<List<PrimerNolPaymentCard>> {
        logSdkFunctionCalls(NolPayAnalyticsConstants.LINKED_CARDS_GET_CARDS_METHOD)
        return linkedCardsDelegate.getLinkedCards(mobileNumber)
    }

    private suspend fun logSdkFunctionCalls(
        methodName: String,
        context: Map<String, String> = hashMapOf(),
    ) = eventLoggingDelegate.logSdkAnalyticsEvent(
        PaymentMethodType.NOL_PAY.name,
        methodName,
        mapOf("category" to PrimerPaymentMethodManagerCategory.NOL_PAY.name).plus(context),
    )

    internal companion object : DISdkComponent {
        fun getInstance() =
            NolPayLinkedCardsComponent(
                linkedCardsDelegate = resolve(),
                eventLoggingDelegate = resolve(PrimerPaymentMethodManagerCategory.NOL_PAY.name),
            )
    }
}
