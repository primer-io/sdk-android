package io.primer.android.vouchers.multibanco.implementation.payment.resume.handler

import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.paymentmethods.common.data.model.ClientTokenIntent
import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeDecision
import io.primer.android.paymentmethods.core.payment.resume.handler.domain.PrimerResumeDecisionHandlerV2
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.vouchers.multibanco.implementation.payment.resume.clientToken.data.MultibancoClientTokenParser
import io.primer.android.vouchers.multibanco.implementation.payment.resume.clientToken.domain.model.MultibancoClientToken
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

internal data class MultibancoDecision(
    val expiresAt: String,
    val reference: String,
    val entity: String,
) : PaymentMethodResumeDecision

internal class MultibancoResumeHandler(
    clientTokenParser: MultibancoClientTokenParser,
    validateClientTokenRepository: ValidateClientTokenRepository,
    clientTokenRepository: ClientTokenRepository,
    checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler,
) : PrimerResumeDecisionHandlerV2<MultibancoDecision, MultibancoClientToken>(
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

    override val supportedClientTokenIntents: () -> List<String> =
        { listOf(ClientTokenIntent.PAYMENT_METHOD_VOUCHER.name) }

    override suspend fun getResumeDecision(clientToken: MultibancoClientToken): MultibancoDecision {
        return MultibancoDecision(
            expiresAt =
            clientToken.expiresAt.let {
                dateFormatISO.parse(it).let { expiresAt ->
                    expiresDateFormat.format(expiresAt)
                }
            },
            reference = clientToken.reference,
            entity = clientToken.entity,
        )
    }
}
