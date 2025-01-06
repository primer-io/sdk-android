package io.primer.android.phoneNumber.implementation.payment.resume.handler

import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeDecision
import io.primer.android.paymentmethods.core.payment.resume.handler.domain.PrimerResumeDecisionHandlerV2
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.phoneNumber.implementation.payment.resume.clientToken.data.PhoneNumberClientTokenParser
import io.primer.android.phoneNumber.implementation.payment.resume.domain.model.PhoneNumberClientToken

internal data class PhoneNumberDecision(
    val statusUrl: String,
) : PaymentMethodResumeDecision

internal class PhoneNumberResumeHandler(
    private val clientTokenParser: PhoneNumberClientTokenParser,
    private val tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    private val validateClientTokenRepository: ValidateClientTokenRepository,
    private val clientTokenRepository: ClientTokenRepository,
    checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler,
) : PrimerResumeDecisionHandlerV2<PhoneNumberDecision, PhoneNumberClientToken>(
        clientTokenRepository = clientTokenRepository,
        validateClientTokenRepository = validateClientTokenRepository,
        clientTokenParser = clientTokenParser,
        checkoutAdditionalInfoHandler = checkoutAdditionalInfoHandler,
    ) {
    override val supportedClientTokenIntents: () -> List<String> = {
        listOf(tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType.orEmpty())
            .map { paymentMethodType -> "${paymentMethodType}_REDIRECTION" }
    }

    override suspend fun getResumeDecision(clientToken: PhoneNumberClientToken): PhoneNumberDecision {
        return PhoneNumberDecision(
            statusUrl = clientToken.statusUrl,
        )
    }
}
