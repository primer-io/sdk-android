package io.primer.android.bancontact.implementation.payment.resume.clientToken.data

import io.primer.android.bancontact.implementation.payment.resume.clientToken.data.model.AdyenBancontactClientTokenData
import io.primer.android.bancontact.implementation.payment.resume.clientToken.domain.model.AdyenBancontactClientToken
import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.parser.PaymentMethodClientTokenParser

internal class AdyenBancontactPaymentMethodClientTokenParser :
    PaymentMethodClientTokenParser<AdyenBancontactClientToken> {

    override fun parseClientToken(clientToken: String): AdyenBancontactClientToken {
        return AdyenBancontactClientTokenData.fromString(clientToken).let { clientTokenData ->
            AdyenBancontactClientToken(
                clientTokenIntent = clientTokenData.intent,
                redirectUrl = clientTokenData.redirectUrl,
                statusUrl = clientTokenData.statusUrl
            )
        }
    }
}
