package io.primer.android.googlepay.implementation.payment.resume.clientToken.data

import io.primer.android.googlepay.implementation.payment.resume.clientToken.data.model.GooglePayNative3DSClientTokenData
import io.primer.android.googlepay.implementation.payment.resume.clientToken.data.model.GooglePayProcessor3DSClientTokenData
import io.primer.android.googlepay.implementation.payment.resume.clientToken.domain.model.GooglePayClientToken
import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.parser.PaymentMethodClientTokenParser
import io.primer.android.processor3ds.domain.model.Processor3DS

internal class GooglePayClientTokenParser :
    PaymentMethodClientTokenParser<GooglePayClientToken> {

    override fun parseClientToken(clientToken: String): GooglePayClientToken {
        return runCatching {
            GooglePayNative3DSClientTokenData.fromString(clientToken).let { clientTokenData ->
                GooglePayClientToken.GooglePayNative3DSClientToken(
                    supportedThreeDsProtocolVersions = clientTokenData.supportedThreeDsProtocolVersions,
                    clientTokenIntent = clientTokenData.intent
                )
            }
        }.getOrNull() ?: GooglePayProcessor3DSClientTokenData.fromString(clientToken).let { clientTokenData ->
            GooglePayClientToken.GooglePayProcessor3DSClientToken(
                clientTokenIntent = clientTokenData.intent,
                processor3DS = Processor3DS(
                    statusUrl = clientTokenData.statusUrl,
                    redirectUrl = clientTokenData.redirectUrl
                )
            )
        }
    }
}
