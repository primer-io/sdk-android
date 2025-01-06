package io.primer.android.stripe.ach.implementation.payment.resume.handler

import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeDecision
import io.primer.android.paymentmethods.core.payment.resume.handler.domain.PrimerResumeDecisionHandlerV2
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.stripe.ach.implementation.payment.resume.clientToken.data.StripeAchPaymentMethodClientTokenParser
import io.primer.android.stripe.ach.implementation.payment.resume.clientToken.domain.model.StripeAchClientToken
import io.primer.android.stripe.ach.implementation.session.data.exception.StripeIllegalValueKey

internal data class StripeAchDecision(
    val sdkCompleteUrl: String,
    val stripePaymentIntentId: String,
    val stripeClientSecret: String,
) : PaymentMethodResumeDecision

internal class StripeAchResumeDecisionHandler(
    clientTokenParser: StripeAchPaymentMethodClientTokenParser,
    tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    validateClientTokenRepository: ValidateClientTokenRepository,
    clientTokenRepository: ClientTokenRepository,
    checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler,
) : PrimerResumeDecisionHandlerV2<StripeAchDecision, StripeAchClientToken>(
        clientTokenRepository = clientTokenRepository,
        validateClientTokenRepository = validateClientTokenRepository,
        checkoutAdditionalInfoHandler = checkoutAdditionalInfoHandler,
        clientTokenParser = clientTokenParser,
    ) {
    override var checkoutAdditionalInfo: PrimerCheckoutAdditionalInfo? = null
        private set

    override val supportedClientTokenIntents: () -> List<String> = {
        listOf(tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType.orEmpty())
            .map { paymentMethodType -> "${paymentMethodType}_REDIRECTION" }
    }

    override suspend fun getResumeDecision(clientToken: StripeAchClientToken): StripeAchDecision {
        return StripeAchDecision(
            sdkCompleteUrl =
                requireNotNullCheck(
                    value = clientToken.sdkCompleteUrl,
                    key = StripeIllegalValueKey.MISSING_COMPLETION_URL,
                ),
            stripePaymentIntentId =
                requireNotNullCheck(
                    value = clientToken.stripePaymentIntentId,
                    key = StripeIllegalValueKey.MISSING_PAYMENT_INTENT_ID,
                ),
            stripeClientSecret =
                requireNotNullCheck(
                    value = clientToken.stripeClientSecret,
                    key = StripeIllegalValueKey.MISSING_CLIENT_SECRET,
                ),
        )
    }
}
