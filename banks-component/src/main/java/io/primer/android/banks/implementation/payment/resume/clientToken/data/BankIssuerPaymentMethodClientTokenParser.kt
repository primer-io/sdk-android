package io.primer.android.banks.implementation.payment.resume.clientToken.data

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.parser.PaymentMethodClientTokenParser
import io.primer.android.banks.implementation.payment.resume.clientToken.data.model.BankIssuerClientTokenData
import io.primer.android.banks.implementation.payment.resume.clientToken.domain.model.BankIssuerClientToken

internal class BankIssuerPaymentMethodClientTokenParser :
    PaymentMethodClientTokenParser<BankIssuerClientToken> {

    override fun parseClientToken(clientToken: String): BankIssuerClientToken {
        return BankIssuerClientTokenData.fromString(clientToken).let { clientTokenData ->
            BankIssuerClientToken(
                clientTokenIntent = clientTokenData.intent,
                redirectUrl = clientTokenData.redirectUrl,
                statusUrl = clientTokenData.statusUrl
            )
        }
    }
}
