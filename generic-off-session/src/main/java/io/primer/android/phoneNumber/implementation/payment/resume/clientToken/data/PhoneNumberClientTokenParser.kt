package io.primer.android.phoneNumber.implementation.payment.resume.clientToken.data

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.parser.PaymentMethodClientTokenParser
import io.primer.android.phoneNumber.implementation.payment.resume.clientToken.data.model.PhoneNumberClientTokenData
import io.primer.android.phoneNumber.implementation.payment.resume.domain.model.PhoneNumberClientToken

internal class PhoneNumberClientTokenParser :
    PaymentMethodClientTokenParser<PhoneNumberClientToken> {

    override fun parseClientToken(clientToken: String): PhoneNumberClientToken {
        with(PhoneNumberClientTokenData.fromString(clientToken)) {
            return PhoneNumberClientToken(
                clientTokenIntent = intent,
                statusUrl = statusUrl
            )
        }
    }
}
