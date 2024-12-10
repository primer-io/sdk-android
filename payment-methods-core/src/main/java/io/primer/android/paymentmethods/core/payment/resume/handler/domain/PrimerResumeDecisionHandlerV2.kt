package io.primer.android.paymentmethods.core.payment.resume.handler.domain

import androidx.annotation.VisibleForTesting
import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.core.extensions.mapSuspendCatching
import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeClientToken
import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeDecision
import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.parser.PaymentMethodClientTokenParser
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler

abstract class PrimerResumeDecisionHandlerV2<T : PaymentMethodResumeDecision, C : PaymentMethodResumeClientToken>(
    private val validateClientTokenRepository: ValidateClientTokenRepository,
    private val clientTokenRepository: ClientTokenRepository,
    private val checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler,
    private val clientTokenParser: PaymentMethodClientTokenParser<C>
) {
    /**
     * The [PrimerCheckoutAdditionalInfo] associated with this payment method. If defined, it will be used after
     * [getResumeDecision] returns.
     */
    protected open val checkoutAdditionalInfo: PrimerCheckoutAdditionalInfo? get() = null

    /**
     * Parses and validates the client token, returning the resume decision. This function will handle
     * [checkoutAdditionalInfo] if it is assigned before [getResumeDecision] returns.
     */
    suspend fun continueWithNewClientToken(clientToken: String): Result<T> {
        return validateClientTokenRepository.validate(clientToken).mapCatching {
            val parsedClientToken: C = clientTokenParser.parseClientToken(clientToken)
            require(
                supportedClientTokenIntents().contains(
                    parsedClientToken.clientTokenIntent
                )
            )
            parsedClientToken
        }.mapSuspendCatching { parsedClientToken ->
            clientTokenRepository.setClientToken(clientToken)
            val decision = getResumeDecision(parsedClientToken)
            checkoutAdditionalInfo?.let { checkoutAdditionalInfoHandler.handle(it) }
            decision
        }
    }

    /**
     * A list of supported client token intents. An exception will be thrown by [continueWithNewClientToken] in case
     * the current token's intent is not supported.
     */
    abstract val supportedClientTokenIntents: () -> List<String>

    /**
     * Returns the resume decision that is used by [continueWithNewClientToken].
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    abstract suspend fun getResumeDecision(clientToken: C): T
}
