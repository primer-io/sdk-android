package io.primer.android.completion

import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.deeplink.async.repository.AsyncPaymentMethodDeeplinkRepository
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.domain.payments.methods.repository.PaymentMethodsRepository
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.domain.token.repository.ValidateTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.model.MonetaryAmount
import io.primer.android.payment.async.ipay88.IPay88CardPaymentMethodDescriptor
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.utils.PaymentUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder

internal class AsyncPaymentPrimerResumeDecisionHandler(
    validationTokenRepository: ValidateTokenRepository,
    private val clientTokenRepository: ClientTokenRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    paymentResultRepository: PaymentResultRepository,
    analyticsRepository: AnalyticsRepository,
    baseErrorEventResolver: BaseErrorEventResolver,
    private val eventDispatcher: EventDispatcher,
    logger: Logger,
    private val config: PrimerConfig,
    private val paymentMethodsRepository: PaymentMethodsRepository,
    retailerOutletRepository: RetailOutletRepository,
    private val asyncPaymentMethodDeeplinkRepository: AsyncPaymentMethodDeeplinkRepository,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DefaultPrimerResumeDecisionHandler(
    validationTokenRepository,
    clientTokenRepository,
    paymentMethodRepository,
    paymentResultRepository,
    analyticsRepository,
    baseErrorEventResolver,
    eventDispatcher,
    logger,
    config,
    paymentMethodsRepository,
    retailerOutletRepository,
    coroutineDispatcher
) {

    override fun handleClientToken(clientToken: String) {
        super.handleClientToken(clientToken)
        val paymentMethodType =
            paymentMethodRepository.getPaymentMethod()
                .paymentInstrumentData?.paymentMethodType.orEmpty()

        when (clientTokenRepository.getClientTokenIntent()) {
            ClientTokenIntent.XFERS_PAYNOW_REDIRECTION.name,
            ClientTokenIntent.RAPYD_FAST_REDIRECTION.name,
            ClientTokenIntent.RAPYD_PROMPTPAY_REDIRECTION.name,
            ClientTokenIntent.OMISE_PROMPTPAY_REDIRECTION.name,
            ClientTokenIntent.ADYEN_BLIK_REDIRECTION.name,
            ClientTokenIntent.ADYEN_MBWAY_REDIRECTION.name,
            ClientTokenIntent.XENDIT_OVO_REDIRECTION.name -> {
                eventDispatcher.dispatchEvents(
                    listOf(
                        CheckoutEvent.PaymentMethodPresented(
                            paymentMethodType
                        ),
                        CheckoutEvent.StartAsyncFlow(
                            clientTokenRepository.getClientTokenIntent(),
                            clientTokenRepository.getStatusUrl().orEmpty(),
                            paymentMethodType,
                        )
                    )
                )
            }
            ClientTokenIntent.PAYMENT_METHOD_VOUCHER.name -> {
                eventDispatcher.dispatchEvents(
                    listOf(
                        CheckoutEvent.PaymentMethodPresented(
                            paymentMethodType
                        ),
                        CheckoutEvent.StartVoucherFlow(
                            clientTokenRepository.getClientTokenIntent(),
                            clientTokenRepository.getStatusUrl().orEmpty(),
                            paymentMethodType
                        )
                    )
                )
            }
            ClientTokenIntent.IPAY88_CARD_REDIRECTION.name -> {
                CoroutineScope(coroutineDispatcher).launch {
                    paymentMethodsRepository.getPaymentMethodDescriptors()
                        .mapLatest { descriptors ->
                            val descriptor = descriptors.firstOrNull { descriptor ->
                                descriptor.config.type ==
                                    paymentMethodRepository.getPaymentMethod().paymentMethodType
                            } as IPay88CardPaymentMethodDescriptor
                            eventDispatcher.dispatchEvents(
                                listOf(
                                    CheckoutEvent.PaymentMethodPresented(
                                        paymentMethodType
                                    ),
                                    CheckoutEvent.StartIPay88Flow(
                                        clientTokenRepository.getClientTokenIntent(),
                                        clientTokenRepository.getStatusUrl().orEmpty(),
                                        paymentMethodType,
                                        descriptor.paymentId,
                                        descriptor.paymentMethod,
                                        requireNotNull(descriptor.config.options?.merchantId),
                                        PaymentUtils.amountToDecimalString(
                                            MonetaryAmount.create(
                                                config.settings.currency,
                                                config.settings.currentAmount
                                            )
                                        ).toString(),
                                        requireNotNull(clientTokenRepository.getTransactionId()),
                                        config.settings.order.let {
                                            it.lineItems.joinToString { it.name.orEmpty() }
                                                .ifEmpty {
                                                    it.lineItems.joinToString {
                                                        it.description.orEmpty()
                                                    }
                                                }
                                        },
                                        config.settings.currency,
                                        config.settings.order.countryCode?.name,
                                        config.settings.customer.let {
                                            "${it.firstName.orEmpty()} ${it.lastName.orEmpty()}"
                                        },
                                        config.settings.customer.emailAddress,
                                        requireNotNull(
                                            withContext(Dispatchers.IO) {
                                                URLEncoder.encode(
                                                    clientTokenRepository.getBackendCallbackUrl(),
                                                    Charsets.UTF_8.name()
                                                )
                                            }
                                        ),
                                        ""
                                    )
                                )
                            )
                        }.collect {}
                }
            }
            else -> eventDispatcher.dispatchEvents(
                listOf(
                    CheckoutEvent.PaymentMethodPresented(paymentMethodType),
                    CheckoutEvent.StartAsyncRedirectFlow(
                        paymentMethodRepository.getPaymentMethod().paymentInstrumentData
                            ?.paymentMethodType?.split("_")?.last().orEmpty(),
                        paymentMethodType,
                        clientTokenRepository.getRedirectUrl().orEmpty(),
                        clientTokenRepository.getStatusUrl().orEmpty(),
                        asyncPaymentMethodDeeplinkRepository.getDeeplinkUrl()
                    )
                )
            )
        }
    }
}
