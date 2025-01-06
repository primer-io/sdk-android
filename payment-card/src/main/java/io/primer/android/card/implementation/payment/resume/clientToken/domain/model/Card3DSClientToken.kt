package io.primer.android.card.implementation.payment.resume.clientToken.domain.model

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeClientToken
import io.primer.android.processor3ds.domain.model.Processor3DS

internal sealed class Card3DSClientToken(
    override val clientTokenIntent: String,
) : PaymentMethodResumeClientToken {
    data class CardNative3DSClientToken(
        override val clientTokenIntent: String,
        val supportedThreeDsProtocolVersions: List<String>,
    ) : Card3DSClientToken(clientTokenIntent = clientTokenIntent)

    data class CardProcessor3DSClientToken(
        override val clientTokenIntent: String,
        val processor3DS: Processor3DS,
    ) : Card3DSClientToken(clientTokenIntent = clientTokenIntent)
}
