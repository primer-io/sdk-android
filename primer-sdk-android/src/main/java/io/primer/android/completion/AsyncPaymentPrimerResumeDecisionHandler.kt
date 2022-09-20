package io.primer.android.completion

import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.domain.payments.methods.repository.PaymentMethodsRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.domain.token.repository.ValidateTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class AsyncPaymentPrimerResumeDecisionHandler(
    validationTokenRepository: ValidateTokenRepository,
    private val clientTokenRepository: ClientTokenRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    paymentResultRepository: PaymentResultRepository,
    analyticsRepository: AnalyticsRepository,
    baseErrorEventResolver: BaseErrorEventResolver,
    private val eventDispatcher: EventDispatcher,
    logger: Logger,
    config: PrimerConfig,
    paymentMethodsRepository: PaymentMethodsRepository,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
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
            ClientTokenIntent.ADYEN_BANCONTACT_CARD_REDIRECTION.name,
            ClientTokenIntent.XENDIT_OVO_REDIRECTION.name -> {
                eventDispatcher.dispatchEvents(
                    listOf(
                        CheckoutEvent.PaymentMethodPresented(
                            paymentMethodType
                        ),
                        CheckoutEvent.StartAsyncFlow(
                            clientTokenRepository.getClientTokenIntent(),
                            clientTokenRepository.getStatusUrl().orEmpty(),
                            paymentMethodType
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
            else -> eventDispatcher.dispatchEvents(
                listOf(
                    CheckoutEvent.PaymentMethodPresented(paymentMethodType),
                    CheckoutEvent.StartAsyncRedirectFlow(
                        paymentMethodRepository.getPaymentMethod().paymentInstrumentData
                            ?.paymentMethodType?.split("_")?.last().orEmpty(),
                        paymentMethodType,
                        clientTokenRepository.getRedirectUrl().orEmpty(),
                        clientTokenRepository.getStatusUrl().orEmpty(),
                    )
                )
            )
        }
    }
}
