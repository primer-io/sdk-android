package io.primer.android.card.implementation.payment.resume.handler

import io.primer.android.card.implementation.payment.resume.clientToken.data.CardNative3DSClientTokenParser
import io.primer.android.card.implementation.payment.resume.clientToken.domain.model.Card3DSClientToken
import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.paymentmethods.common.data.model.ClientTokenIntent
import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeDecision
import io.primer.android.paymentmethods.core.payment.resume.handler.domain.PrimerResumeDecisionHandlerV2
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.processor3ds.domain.model.Processor3DS

internal sealed interface CardResumeDecision : PaymentMethodResumeDecision {
    data class CardNative3dsResumeDecision(val supportedThreeDsProtocolVersions: List<String>) : CardResumeDecision

    data class CardProcessor3dsResumeDecision(val processor3DS: Processor3DS) : CardResumeDecision
}

internal class CardResumeHandler(
    clientTokenParser: CardNative3DSClientTokenParser,
    validateClientTokenRepository: ValidateClientTokenRepository,
    clientTokenRepository: ClientTokenRepository,
    checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler,
) : PrimerResumeDecisionHandlerV2<CardResumeDecision, Card3DSClientToken>(
        clientTokenRepository = clientTokenRepository,
        validateClientTokenRepository = validateClientTokenRepository,
        clientTokenParser = clientTokenParser,
        checkoutAdditionalInfoHandler = checkoutAdditionalInfoHandler,
    ) {
    override val supportedClientTokenIntents: () -> List<String> =
        { listOf(ClientTokenIntent.`3DS_AUTHENTICATION`.name, ClientTokenIntent.PROCESSOR_3DS.name) }

    override suspend fun getResumeDecision(clientToken: Card3DSClientToken): CardResumeDecision {
        return when (clientToken) {
            is Card3DSClientToken.CardNative3DSClientToken ->
                CardResumeDecision.CardNative3dsResumeDecision(
                    supportedThreeDsProtocolVersions = clientToken.supportedThreeDsProtocolVersions,
                )

            is Card3DSClientToken.CardProcessor3DSClientToken ->
                CardResumeDecision.CardProcessor3dsResumeDecision(
                    processor3DS = clientToken.processor3DS,
                )
        }
    }
}
