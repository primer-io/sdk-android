package io.primer.android.otp.implementation.payment.resume.clientToken.data

import io.primer.android.otp.implementation.payment.resume.clientToken.data.model.OtpClientTokenData
import io.primer.android.otp.implementation.payment.resume.domain.model.OtpClientToken
import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.parser.PaymentMethodClientTokenParser

internal class OtpClientTokenParser :
    PaymentMethodClientTokenParser<OtpClientToken> {
    override fun parseClientToken(clientToken: String): OtpClientToken {
        with(OtpClientTokenData.fromString(clientToken)) {
            return OtpClientToken(
                clientTokenIntent = intent,
                statusUrl = statusUrl,
            )
        }
    }
}
