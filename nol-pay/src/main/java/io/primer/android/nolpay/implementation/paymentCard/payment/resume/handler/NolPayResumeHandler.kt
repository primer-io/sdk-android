package io.primer.android.nolpay.implementation.paymentCard.payment.resume.handler

import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.nolpay.implementation.paymentCard.payment.resume.clientToken.data.NolPayClientTokenParser
import io.primer.android.nolpay.implementation.paymentCard.payment.resume.clientToken.domain.model.NolPayClientToken
import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeDecision
import io.primer.android.paymentmethods.core.payment.resume.handler.domain.PrimerResumeDecisionHandlerV2
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository

internal data class NolPayResumeDecision(
    val transactionNumber: String,
    val statusUrl: String,
    val completeUrl: String
) : PaymentMethodResumeDecision

internal class NolPayResumeHandler(
    clientTokenParser: NolPayClientTokenParser,
    validateClientTokenRepository: ValidateClientTokenRepository,
    clientTokenRepository: ClientTokenRepository,
    checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler,
    private val tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository
) : PrimerResumeDecisionHandlerV2<NolPayResumeDecision, NolPayClientToken>(
    clientTokenRepository = clientTokenRepository,
    validateClientTokenRepository = validateClientTokenRepository,
    clientTokenParser = clientTokenParser,
    checkoutAdditionalInfoHandler = checkoutAdditionalInfoHandler
) {

    override val supportedClientTokenIntents: () -> List<String> = {
        listOf(tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType.orEmpty())
            .map { paymentMethodType -> "${paymentMethodType}_REDIRECTION" }
    }

    override suspend fun getResumeDecision(clientToken: NolPayClientToken): NolPayResumeDecision {
        return NolPayResumeDecision(
            transactionNumber = clientToken.transactionNumber,
            statusUrl = clientToken.statusUrl,
            completeUrl = clientToken.completeUrl
        )
    }
}
