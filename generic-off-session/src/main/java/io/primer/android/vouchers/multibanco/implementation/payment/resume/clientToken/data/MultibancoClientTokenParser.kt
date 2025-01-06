package io.primer.android.vouchers.multibanco.implementation.payment.resume.clientToken.data

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.parser.PaymentMethodClientTokenParser
import io.primer.android.vouchers.multibanco.implementation.payment.resume.clientToken.data.model.MultibancoClientTokenData
import io.primer.android.vouchers.multibanco.implementation.payment.resume.clientToken.domain.model.MultibancoClientToken

internal class MultibancoClientTokenParser :
    PaymentMethodClientTokenParser<MultibancoClientToken> {
    override fun parseClientToken(clientToken: String): MultibancoClientToken {
        return MultibancoClientTokenData.fromString(clientToken).let { clientTokenData ->
            MultibancoClientToken(
                clientTokenIntent = clientTokenData.intent,
                expiresAt = clientTokenData.expiresAt,
                entity = clientTokenData.entity,
                reference = clientTokenData.reference,
            )
        }
    }
}
