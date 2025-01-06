package io.primer.android.ipay88.implementation.payment.resume.handler

import io.primer.android.PrimerSessionIntent
import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.utils.BaseDataWithInputProvider
import io.primer.android.data.settings.internal.MonetaryAmount
import io.primer.android.errors.data.exception.IllegalClientSessionValueException
import io.primer.android.ipay88.implementation.deeplink.domain.repository.IPay88DeeplinkRepository
import io.primer.android.ipay88.implementation.payment.resume.clientToken.data.IPay88ClientTokenParser
import io.primer.android.ipay88.implementation.payment.resume.clientToken.domain.model.IPay88ClientToken
import io.primer.android.ipay88.implementation.validation.IPay88ValidationData
import io.primer.android.ipay88.implementation.validation.resolvers.IPay88ValidationRulesResolver
import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeDecision
import io.primer.android.paymentmethods.core.payment.resume.handler.domain.PrimerResumeDecisionHandlerV2
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository

internal data class IPay88Decision(
    val statusUrl: String,
    val iPayPaymentId: String,
    val iPayMethod: Int,
    val merchantCode: String,
    val actionType: String?,
    val amount: String,
    val referenceNumber: String,
    val prodDesc: String,
    val currencyCode: String?,
    val countryCode: String?,
    val customerName: String?,
    val customerEmail: String?,
    val remark: String?,
    val backendCallbackUrl: String,
    val deeplinkUrl: String,
    val errorCode: Int,
    val paymentMethodType: String,
    val sessionIntent: PrimerSessionIntent,
) : PaymentMethodResumeDecision

internal class IPay88ResumeHandler(
    private val iPay88DeeplinkRepository: IPay88DeeplinkRepository,
    private val iPay88ValidationRulesResolver: IPay88ValidationRulesResolver,
    clientTokenParser: IPay88ClientTokenParser,
    private val tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    private val configurationRepository: ConfigurationRepository,
    validateClientTokenRepository: ValidateClientTokenRepository,
    private val formattedAmountProvider: BaseDataWithInputProvider<MonetaryAmount, String>,
    clientTokenRepository: ClientTokenRepository,
    checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler,
) : PrimerResumeDecisionHandlerV2<IPay88Decision, IPay88ClientToken>(
        clientTokenRepository = clientTokenRepository,
        validateClientTokenRepository = validateClientTokenRepository,
        clientTokenParser = clientTokenParser,
        checkoutAdditionalInfoHandler = checkoutAdditionalInfoHandler,
    ) {
    override val supportedClientTokenIntents: () -> List<String> = {
        listOf(tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType.orEmpty())
            .map { paymentMethodType -> "${paymentMethodType}_REDIRECTION" }
    }

    @Throws(IllegalClientSessionValueException::class)
    override suspend fun getResumeDecision(clientToken: IPay88ClientToken): IPay88Decision {
        iPay88ValidationRulesResolver.resolve().rules.map {
            it.validate(
                IPay88ValidationData(
                    clientSession =
                        configurationRepository.getConfiguration().clientSession
                            .clientSessionDataResponse.toClientSessionData(),
                    clientToken = clientToken,
                ),
            )
        }.filterIsInstance<ValidationResult.Failure>().forEach {
            throw it.exception
        }

        val customer = configurationRepository.getConfiguration().clientSession.clientSessionDataResponse.customer
        val order =
            requireNotNull(configurationRepository.getConfiguration().clientSession.clientSessionDataResponse.order)

        val amount =
            MonetaryAmount.create(
                order.currencyCode,
                order.currentAmount,
            )

        val amountString = amount?.let { formattedAmountProvider.provide(input = amount) }.orEmpty()

        val paymentMethodType = tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType
        val merchantId =
            requireNotNull(
                configurationRepository.getConfiguration().paymentMethods.first { paymentMethodConfig ->
                    paymentMethodConfig.type == paymentMethodType
                }.options?.merchantId,
            )

        return IPay88Decision(
            statusUrl = clientToken.statusUrl,
            iPayPaymentId = clientToken.paymentId,
            iPayMethod = clientToken.paymentMethod,
            merchantCode = merchantId,
            actionType = clientToken.actionType,
            amount = amountString,
            referenceNumber = clientToken.referenceNumber,
            prodDesc = order.lineItems.joinToString { it.description.orEmpty() },
            currencyCode = order.currencyCode,
            countryCode = order.countryCode?.name,
            customerName = customer?.getFullName(),
            customerEmail = customer?.emailAddress,
            remark = customer?.customerId,
            backendCallbackUrl = clientToken.backendCallbackUrl,
            deeplinkUrl = iPay88DeeplinkRepository.getDeeplinkUrl(),
            errorCode = RESULT_ERROR_CODE,
            paymentMethodType = tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType.orEmpty(),
            sessionIntent = PrimerSessionIntent.CHECKOUT,
        )
    }

    companion object {
        const val RESULT_ERROR_CODE = 1234
    }
}
