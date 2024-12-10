package io.primer.android.ipay88.implementation.payment.resume.clientToken.data

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.parser.PaymentMethodClientTokenParser
import io.primer.android.ipay88.implementation.payment.resume.clientToken.data.model.IPay88ClientTokenData
import io.primer.android.ipay88.implementation.payment.resume.clientToken.domain.model.IPay88ClientToken

internal class IPay88ClientTokenParser :
    PaymentMethodClientTokenParser<IPay88ClientToken> {

    override fun parseClientToken(clientToken: String): IPay88ClientToken {
        return IPay88ClientTokenData.fromString(clientToken).let { clientTokenData ->
            IPay88ClientToken(
                intent = clientTokenData.intent,
                statusUrl = clientTokenData.statusUrl,
                paymentId = clientTokenData.paymentId,
                paymentMethod = clientTokenData.paymentMethod,
                actionType = clientTokenData.actionType,
                referenceNumber = clientTokenData.referenceNumber,
                supportedCurrencyCode = clientTokenData.currencyCode,
                backendCallbackUrl = clientTokenData.backendCallbackUrl,
                supportedCountryCode = clientTokenData.countryCode,
                clientTokenIntent = clientTokenData.intent
            )
        }
    }
}
