package io.primer.android.otp.implementation.payment.resume.handler

import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.otp.implementation.payment.resume.clientToken.data.OtpClientTokenParser
import io.primer.android.otp.implementation.payment.resume.domain.model.OtpClientToken
import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeDecision
import io.primer.android.paymentmethods.core.payment.resume.handler.domain.PrimerResumeDecisionHandlerV2
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository

internal data class OtpDecision(
    val statusUrl: String,
) : PaymentMethodResumeDecision

internal class OtpResumeHandler(
    private val clientTokenParser: OtpClientTokenParser,
    private val tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    private val validateClientTokenRepository: ValidateClientTokenRepository,
    private val clientTokenRepository: ClientTokenRepository,
    checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler,
) : PrimerResumeDecisionHandlerV2<OtpDecision, OtpClientToken>(
    clientTokenRepository = clientTokenRepository,
    validateClientTokenRepository = validateClientTokenRepository,
    clientTokenParser = clientTokenParser,
    checkoutAdditionalInfoHandler = checkoutAdditionalInfoHandler,
) {
    override val supportedClientTokenIntents: () -> List<String> = {
        listOf(tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType.orEmpty())
            .map { paymentMethodType -> "${paymentMethodType}_REDIRECTION" }
    }

    override suspend fun getResumeDecision(clientToken: OtpClientToken): OtpDecision {
        return OtpDecision(
            statusUrl = clientToken.statusUrl,
        )
    }
}
