package io.primer.android.stripe.ach.implementation.payment.resume.clientToken.data

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.parser.PaymentMethodClientTokenParser
import io.primer.android.stripe.ach.implementation.payment.resume.clientToken.data.model.StripeAchClientTokenData
import io.primer.android.stripe.ach.implementation.payment.resume.clientToken.domain.model.StripeAchClientToken

internal class StripeAchPaymentMethodClientTokenParser :
    PaymentMethodClientTokenParser<StripeAchClientToken> {

    override fun parseClientToken(clientToken: String): StripeAchClientToken {
        return StripeAchClientTokenData.fromString(clientToken).let { clientTokenData ->
            StripeAchClientToken(
                sdkCompleteUrl = clientTokenData.sdkCompleteUrl,
                stripePaymentIntentId = clientTokenData.stripePaymentIntentId,
                stripeClientSecret = clientTokenData.stripeClientSecret,
                clientTokenIntent = clientTokenData.intent
            )
        }
    }
}
