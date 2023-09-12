package io.primer.android.payment.async.ipay88.resume

import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.completion.DefaultPrimerResumeDecisionHandler
import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.IPay88ValidationData
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.resolvers.IPay88ValidationRulesResolver
import io.primer.android.data.base.exceptions.IllegalClientSessionValueException
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.model.ClientToken
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.deeplink.ipay88.repository.IPay88DeeplinkRepository
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.domain.payments.methods.repository.PaymentMethodDescriptorsRepository
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
import io.primer.android.domain.session.repository.ConfigurationRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.domain.token.repository.ValidateTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.model.MonetaryAmount
import io.primer.android.payment.async.ipay88.IPay88PaymentMethodDescriptor
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.utils.PaymentUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.net.URLEncoder

@Suppress("LongParameterList")
internal class IPay88ResumeDecisionHandler(
    private val iPay88DeeplinkRepository: IPay88DeeplinkRepository,
    private val iPay88ValidationRulesResolver: IPay88ValidationRulesResolver,
    private val configurationRepository: ConfigurationRepository,
    validationTokenRepository: ValidateTokenRepository,
    private val clientTokenRepository: ClientTokenRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    paymentResultRepository: PaymentResultRepository,
    analyticsRepository: AnalyticsRepository,
    errorEventResolver: BaseErrorEventResolver,
    private val eventDispatcher: EventDispatcher,
    logger: Logger,
    private val config: PrimerConfig,
    private val paymentMethodDescriptorsRepository: PaymentMethodDescriptorsRepository,
    retailerOutletRepository: RetailOutletRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : DefaultPrimerResumeDecisionHandler(
    validationTokenRepository,
    clientTokenRepository,
    paymentMethodRepository,
    paymentResultRepository,
    analyticsRepository,
    errorEventResolver,
    eventDispatcher,
    logger,
    config,
    paymentMethodDescriptorsRepository,
    retailerOutletRepository,
    dispatcher
) {

    @Throws(IllegalClientSessionValueException::class)
    @Suppress("LongMethod")
    override fun handleClientToken(clientToken: String) {
        super.handleClientToken(clientToken)
        iPay88ValidationRulesResolver.resolve().rules.map {
            it.validate(
                IPay88ValidationData(
                    configurationRepository.getConfiguration().clientSession
                        ?.clientSession?.toClientSessionData(),
                    ClientToken.fromString(clientToken)
                )
            )
        }.filterIsInstance<ValidationResult.Failure>().forEach {
            throw it.exception
        }
        val descriptor =
            paymentMethodDescriptorsRepository.getPaymentMethodDescriptors()
                .firstOrNull { descriptor ->
                    descriptor.config.type ==
                        paymentMethodRepository.getPaymentMethod().paymentMethodType
                } as IPay88PaymentMethodDescriptor

        val paymentMethodType =
            paymentMethodRepository.getPaymentMethod()
                .paymentInstrumentData?.paymentMethodType.orEmpty()

        eventDispatcher.dispatchEvents(
            listOf(
                CheckoutEvent.StartIPay88Flow(
                    clientTokenRepository.getClientTokenIntent(),
                    clientTokenRepository.getStatusUrl().orEmpty(),
                    paymentMethodType,
                    requireNotNull(clientTokenRepository.getPaymentMethodId()),
                    descriptor.paymentMethod,
                    requireNotNull(descriptor.config.options?.merchantId),
                    clientTokenRepository.getActionType().orEmpty(),
                    PaymentUtils.amountToDecimalString(
                        MonetaryAmount.create(
                            config.settings.currency,
                            config.settings.currentAmount
                        )
                    ).toString(),
                    requireNotNull(clientTokenRepository.getTransactionId()),
                    config.settings.order.let { order ->
                        order.lineItems.joinToString { it.description.orEmpty() }
                    },
                    config.settings.currency,
                    config.settings.order.countryCode?.name,
                    config.settings.customer.getFullName(),
                    config.settings.customer.emailAddress,
                    config.settings.customer.customerId,
                    requireNotNull(
                        URLEncoder.encode(
                            clientTokenRepository.getBackendCallbackUrl(),
                            Charsets.UTF_8.name()
                        )
                    ),
                    iPay88DeeplinkRepository.getDeeplinkUrl(),
                    config.paymentMethodIntent
                )
            )
        )
    }
}
