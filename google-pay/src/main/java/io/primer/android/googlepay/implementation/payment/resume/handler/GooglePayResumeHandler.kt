package io.primer.android.googlepay.implementation.payment.resume.handler

import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.googlepay.implementation.payment.resume.clientToken.data.GooglePayClientTokenParser
import io.primer.android.googlepay.implementation.payment.resume.clientToken.domain.model.GooglePayClientToken
import io.primer.android.paymentmethods.common.data.model.ClientTokenIntent
import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeDecision
import io.primer.android.paymentmethods.core.payment.resume.handler.domain.PrimerResumeDecisionHandlerV2
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.processor3ds.domain.model.Processor3DS

internal sealed interface GooglePayResumeDecision : PaymentMethodResumeDecision {
    data class GooglePayNative3dsResumeDecision(val supportedThreeDsProtocolVersions: List<String>) :
        GooglePayResumeDecision

    data class GooglePayProcessor3dsResumeDecision(val processor3DS: Processor3DS) : GooglePayResumeDecision
}

internal class GooglePayResumeHandler(
    clientTokenParser: GooglePayClientTokenParser,
    validateClientTokenRepository: ValidateClientTokenRepository,
    clientTokenRepository: ClientTokenRepository,
    checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler,
) : PrimerResumeDecisionHandlerV2<GooglePayResumeDecision, GooglePayClientToken>(
        clientTokenRepository = clientTokenRepository,
        validateClientTokenRepository = validateClientTokenRepository,
        clientTokenParser = clientTokenParser,
        checkoutAdditionalInfoHandler = checkoutAdditionalInfoHandler,
    ) {
    override val supportedClientTokenIntents: () -> List<String> =
        { listOf(ClientTokenIntent.`3DS_AUTHENTICATION`.name, ClientTokenIntent.PROCESSOR_3DS.name) }

    override suspend fun getResumeDecision(clientToken: GooglePayClientToken): GooglePayResumeDecision {
        return when (clientToken) {
            is GooglePayClientToken.GooglePayNative3DSClientToken ->
                GooglePayResumeDecision.GooglePayNative3dsResumeDecision(
                    supportedThreeDsProtocolVersions = clientToken.supportedThreeDsProtocolVersions,
                )

            is GooglePayClientToken.GooglePayProcessor3DSClientToken ->
                GooglePayResumeDecision.GooglePayProcessor3dsResumeDecision(
                    processor3DS = clientToken.processor3DS,
                )
        }
    }
}
