package io.primer.android.qrcode.implementation.payment.resume.handler

import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeDecision
import io.primer.android.paymentmethods.core.payment.resume.handler.domain.PrimerResumeDecisionHandlerV2
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.qrcode.implementation.payment.resume.clientToken.data.QrCodeClientTokenParser
import io.primer.android.qrcode.implementation.payment.resume.domain.model.QrCodeClientToken
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

internal data class QrCodeDecision(
    val statusUrl: String,
    val expiresAt: String?,
    val qrCodeUrl: String?,
    val qrCodeBase64: String,
) : PaymentMethodResumeDecision

internal class QrCodeResumeHandler(
    private val clientTokenParser: QrCodeClientTokenParser,
    private val tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    private val validateClientTokenRepository: ValidateClientTokenRepository,
    private val clientTokenRepository: ClientTokenRepository,
    checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler,
) : PrimerResumeDecisionHandlerV2<QrCodeDecision, QrCodeClientToken>(
        clientTokenRepository = clientTokenRepository,
        validateClientTokenRepository = validateClientTokenRepository,
        clientTokenParser = clientTokenParser,
        checkoutAdditionalInfoHandler = checkoutAdditionalInfoHandler,
    ) {
    private val dateFormatISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val expiresDateFormat =
        DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM,
            DateFormat.SHORT,
        )

    override val supportedClientTokenIntents: () -> List<String> = {
        listOf(tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType.orEmpty())
            .map { paymentMethodType -> "${paymentMethodType}_REDIRECTION" }
    }

    override suspend fun getResumeDecision(clientToken: QrCodeClientToken): QrCodeDecision {
        return QrCodeDecision(
            statusUrl = clientToken.statusUrl,
            expiresAt =
                clientToken.expiresAt?.let {
                    dateFormatISO.parse(it)?.let { parsedExpiresAt ->
                        expiresDateFormat.format(parsedExpiresAt)
                    }
                },
            qrCodeUrl = clientToken.qrCodeUrl,
            qrCodeBase64 = clientToken.qrCodeBase64,
        )
    }
}
