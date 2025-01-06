package io.primer.android.nolpay.implementation.paymentCard.payment.resume.clientToken.data

import io.primer.android.nolpay.implementation.paymentCard.payment.resume.clientToken.data.model.NolPayClientTokenData
import io.primer.android.nolpay.implementation.paymentCard.payment.resume.clientToken.domain.model.NolPayClientToken
import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.parser.PaymentMethodClientTokenParser

internal class NolPayClientTokenParser : PaymentMethodClientTokenParser<NolPayClientToken> {
    override fun parseClientToken(clientToken: String): NolPayClientToken {
        return NolPayClientTokenData.fromString(clientToken).let { clientTokenData ->
            NolPayClientToken(
                clientTokenIntent = clientTokenData.intent,
                transactionNumber = clientTokenData.transactionNumber,
                statusUrl = clientTokenData.statusUrl,
                completeUrl = clientTokenData.completeUrl,
            )
        }
    }
}
