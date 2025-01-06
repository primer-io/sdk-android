package io.primer.android.webredirect.implementation.payment.resume.clientToken.data

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.parser.PaymentMethodClientTokenParser
import io.primer.android.webredirect.implementation.payment.resume.clientToken.data.model.WebRedirectClientTokenData
import io.primer.android.webredirect.implementation.payment.resume.clientToken.domain.model.WebRedirectClientToken

internal class WebRedirectPaymentMethodClientTokenParser :
    PaymentMethodClientTokenParser<WebRedirectClientToken> {
    override fun parseClientToken(clientToken: String): WebRedirectClientToken {
        return WebRedirectClientTokenData.fromString(clientToken).let { clientTokenData ->
            WebRedirectClientToken(
                clientTokenIntent = clientTokenData.intent,
                redirectUrl = clientTokenData.redirectUrl,
                statusUrl = clientTokenData.statusUrl,
            )
        }
    }
}
