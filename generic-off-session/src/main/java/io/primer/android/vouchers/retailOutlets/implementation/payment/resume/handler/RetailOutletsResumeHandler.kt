package io.primer.android.vouchers.retailOutlets.implementation.payment.resume.handler

import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.paymentmethods.common.data.model.ClientTokenIntent
import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeDecision
import io.primer.android.paymentmethods.core.payment.resume.handler.domain.PrimerResumeDecisionHandlerV2
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.vouchers.retailOutlets.XenditCheckoutVoucherAdditionalInfo
import io.primer.android.vouchers.retailOutlets.implementation.payment.resume.clientToken.data.RetailOutletsClientTokenParser
import io.primer.android.vouchers.retailOutlets.implementation.payment.resume.clientToken.domain.model.RetailOutletsClientToken
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.repository.RetailOutletRepository
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

internal data class RetailOutletsDecision(
    val expiresAt: String,
    val reference: String,
    val entity: String,
    val retailerName: String,
) : PaymentMethodResumeDecision

internal class RetailOutletsResumeHandler(
    clientTokenParser: RetailOutletsClientTokenParser,
    validateClientTokenRepository: ValidateClientTokenRepository,
    clientTokenRepository: ClientTokenRepository,
    private val retailOutletRepository: RetailOutletRepository,
    private val tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler,
) : PrimerResumeDecisionHandlerV2<RetailOutletsDecision, RetailOutletsClientToken>(
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
    override var checkoutAdditionalInfo: PrimerCheckoutAdditionalInfo? = null

    override val supportedClientTokenIntents: () -> List<String> =
        { listOf(ClientTokenIntent.PAYMENT_METHOD_VOUCHER.name) }

    override suspend fun getResumeDecision(clientToken: RetailOutletsClientToken): RetailOutletsDecision {
        return RetailOutletsDecision(
            expiresAt =
            clientToken.expiresAt.let {
                dateFormatISO.parse(it).let { expiresAt ->
                    expiresDateFormat.format(expiresAt)
                }
            },
            reference = clientToken.reference,
            entity = clientToken.entity,
            retailerName =
            retailOutletRepository.getCachedRetailOutlets().first { retailOutlet ->
                retailOutlet.id ==
                    tokenizedPaymentMethodRepository.getPaymentMethod()
                        .paymentInstrumentData?.sessionInfo?.retailOutlet
            }.name,
        ).also { decision ->
            checkoutAdditionalInfo =
                XenditCheckoutVoucherAdditionalInfo(
                    expiresAt = decision.expiresAt,
                    couponCode = decision.reference,
                    retailerName = decision.retailerName,
                )
        }
    }
}
