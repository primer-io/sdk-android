package io.primer.android.googlepay.implementation.payment.resume.clientToken.domain.model

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeClientToken
import io.primer.android.processor3ds.domain.model.Processor3DS

internal sealed class GooglePayClientToken(
    override val clientTokenIntent: String
) : PaymentMethodResumeClientToken {

    data class GooglePayNative3DSClientToken(
        override val clientTokenIntent: String,
        val supportedThreeDsProtocolVersions: List<String>
    ) : GooglePayClientToken(clientTokenIntent = clientTokenIntent)

    data class GooglePayProcessor3DSClientToken(
        override val clientTokenIntent: String,
        val processor3DS: Processor3DS
    ) : GooglePayClientToken(clientTokenIntent = clientTokenIntent)
}
