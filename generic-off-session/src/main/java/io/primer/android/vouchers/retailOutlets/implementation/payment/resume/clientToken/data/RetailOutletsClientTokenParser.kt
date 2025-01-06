package io.primer.android.vouchers.retailOutlets.implementation.payment.resume.clientToken.data

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.parser.PaymentMethodClientTokenParser
import io.primer.android.vouchers.retailOutlets.implementation.payment.resume.clientToken.data.model.RetailOutletsClientTokenData
import io.primer.android.vouchers.retailOutlets.implementation.payment.resume.clientToken.domain.model.RetailOutletsClientToken

internal class RetailOutletsClientTokenParser :
    PaymentMethodClientTokenParser<RetailOutletsClientToken> {
    override fun parseClientToken(clientToken: String): RetailOutletsClientToken {
        return RetailOutletsClientTokenData.fromString(clientToken).let { clientTokenData ->
            RetailOutletsClientToken(
                clientTokenIntent = clientTokenData.intent,
                expiresAt = clientTokenData.expiresAt,
                entity = clientTokenData.entity,
                reference = clientTokenData.reference,
            )
        }
    }
}
