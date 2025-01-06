package io.primer.android.qrcode.implementation.payment.resume.clientToken.data

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.parser.PaymentMethodClientTokenParser
import io.primer.android.qrcode.implementation.payment.resume.clientToken.data.model.QrCodeClientTokenData
import io.primer.android.qrcode.implementation.payment.resume.domain.model.QrCodeClientToken

internal class QrCodeClientTokenParser :
    PaymentMethodClientTokenParser<QrCodeClientToken> {
    override fun parseClientToken(clientToken: String): QrCodeClientToken {
        return QrCodeClientTokenData.fromString(clientToken).let { clientTokenData ->
            QrCodeClientToken(
                clientTokenIntent = clientTokenData.intent,
                statusUrl = clientTokenData.statusUrl,
                expiresAt = clientTokenData.expiresAt,
                qrCodeUrl = clientTokenData.qrCodeUrl,
                qrCodeBase64 = clientTokenData.qrCodeBase64,
            )
        }
    }
}
