package io.primer.android.components.manager.nolPay.listCards.component

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.manager.core.composable.PrimerHeadlessComponent
import io.primer.android.components.manager.nolPay.analytics.NolPayAnalyticsConstants
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayGetLinkedCardsDelegate
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.resolve
import io.primer.nolpay.api.models.PrimerNolPaymentCard

class NolPayLinkedCardsComponent internal constructor(
    private val linkedCardsDelegate: NolPayGetLinkedCardsDelegate
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
    suspend fun getLinkedCards(
        mobileNumber: String
    ): Result<List<PrimerNolPaymentCard>> {
        logSdkFunctionCalls(NolPayAnalyticsConstants.LINKED_CARDS_GET_CARDS_METHOD)
        return linkedCardsDelegate.getLinkedCards(mobileNumber)
    }

    private suspend fun logSdkFunctionCalls(
        methodName: String,
        context: Map<String, String> = hashMapOf()
    ) = linkedCardsDelegate.logSdkAnalyticsEvent(
        methodName,
        mapOf("category" to PrimerPaymentMethodManagerCategory.NOL_PAY.name).plus(context)
    )

    internal companion object : DISdkComponent {

        fun getInstance() = NolPayLinkedCardsComponent(resolve())
    }
}
