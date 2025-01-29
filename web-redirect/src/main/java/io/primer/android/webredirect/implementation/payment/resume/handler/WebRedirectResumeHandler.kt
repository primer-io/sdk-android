package io.primer.android.webredirect.implementation.payment.resume.handler

import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeDecision
import io.primer.android.paymentmethods.core.payment.resume.handler.domain.PrimerResumeDecisionHandlerV2
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.webRedirectShared.implementation.deeplink.domain.repository.RedirectDeeplinkRepository
import io.primer.android.webredirect.implementation.payment.resume.clientToken.data.WebRedirectPaymentMethodClientTokenParser
import io.primer.android.webredirect.implementation.payment.resume.clientToken.domain.model.WebRedirectClientToken

internal data class WebRedirectDecision(
    val title: String,
    val paymentMethodType: String,
    val redirectUrl: String,
    val statusUrl: String,
    val deeplinkUrl: String,
) : PaymentMethodResumeDecision

internal class WebRedirectResumeHandler(
    clientTokenParser: WebRedirectPaymentMethodClientTokenParser,
    private val tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    private val configurationRepository: ConfigurationRepository,
    private val deeplinkRepository: RedirectDeeplinkRepository,
    validateClientTokenRepository: ValidateClientTokenRepository,
    clientTokenRepository: ClientTokenRepository,
    checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler,
) : PrimerResumeDecisionHandlerV2<WebRedirectDecision, WebRedirectClientToken>(
    clientTokenRepository = clientTokenRepository,
    validateClientTokenRepository = validateClientTokenRepository,
    clientTokenParser = clientTokenParser,
    checkoutAdditionalInfoHandler = checkoutAdditionalInfoHandler,
) {
    override val supportedClientTokenIntents: () -> List<String> = {
        listOf(tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType.orEmpty())
            .map { paymentMethodType -> "${paymentMethodType}_REDIRECTION" }
    }

    override suspend fun getResumeDecision(clientToken: WebRedirectClientToken): WebRedirectDecision {
        return WebRedirectDecision(
            title =
            configurationRepository.getConfiguration().paymentMethods.find { config ->
                config.type == tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType
            }?.name.orEmpty(),
            paymentMethodType =
            requireNotNull(
                tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType,
            ),
            redirectUrl = clientToken.redirectUrl,
            statusUrl = clientToken.statusUrl,
            deeplinkUrl = deeplinkRepository.getDeeplinkUrl(),
        )
    }
}
