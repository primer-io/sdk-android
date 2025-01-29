package io.primer.android.card.implementation.payment.resume.clientToken.data

import io.primer.android.card.implementation.payment.resume.clientToken.data.model.CardNative3DSClientTokenData
import io.primer.android.card.implementation.payment.resume.clientToken.data.model.CardProcessor3dsClientTokenData
import io.primer.android.card.implementation.payment.resume.clientToken.domain.model.Card3DSClientToken
import io.primer.android.clientToken.core.token.data.model.ClientToken
import io.primer.android.paymentmethods.common.data.model.ClientTokenIntent
import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.parser.PaymentMethodClientTokenParser
import io.primer.android.processor3ds.domain.model.Processor3DS

internal class CardNative3DSClientTokenParser :
    PaymentMethodClientTokenParser<Card3DSClientToken> {
    override fun parseClientToken(clientToken: String): Card3DSClientToken {
        return when (val clientTokenIntent = ClientToken.fromString(clientToken).intent) {
            ClientTokenIntent.`3DS_AUTHENTICATION`.name ->
                CardNative3DSClientTokenData.fromString(clientToken).let { clientTokenData ->
                    Card3DSClientToken.CardNative3DSClientToken(
                        supportedThreeDsProtocolVersions = clientTokenData.supportedThreeDsProtocolVersions,
                        clientTokenIntent = clientTokenData.intent,
                    )
                }

            ClientTokenIntent.PROCESSOR_3DS.name ->
                CardProcessor3dsClientTokenData.fromString(clientToken).let { clientTokenData ->
                    Card3DSClientToken.CardProcessor3DSClientToken(
                        clientTokenIntent = clientTokenData.intent,
                        processor3DS =
                        Processor3DS(
                            statusUrl = clientTokenData.statusUrl,
                            redirectUrl = clientTokenData.redirectUrl,
                        ),
                    )
                }

            else -> error("Unsupported client token intent: $clientTokenIntent")
        }
    }
}
