package io.primer.android.completion

import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.token.repository.ValidateTokenRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.model.dto.PaymentMethodType
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class AsyncPaymentResumeDecisionHandler(
    validationTokenRepository: ValidateTokenRepository,
    private val clientTokenRepository: ClientTokenRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    analyticsRepository: AnalyticsRepository,
    baseErrorEventResolver: BaseErrorEventResolver,
    private val eventDispatcher: EventDispatcher,
    logger: Logger,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DefaultResumeDecisionHandler(
    validationTokenRepository,
    clientTokenRepository,
    paymentMethodRepository,
    analyticsRepository,
    baseErrorEventResolver,
    eventDispatcher,
    logger,
    coroutineDispatcher
) {

    override fun handleClientToken(clientToken: String) {
        super.handleClientToken(clientToken)
        when (clientTokenRepository.getClientTokenIntent()) {
            ClientTokenIntent.XFERS_PAYNOW_REDIRECTION,
            ClientTokenIntent.ADYEN_BLIK_REDIRECTION -> {
                eventDispatcher.dispatchEvents(
                    listOf(
                        CheckoutEvent.PaymentMethodPresented,
                        CheckoutEvent.StartAsyncFlow(
                            clientTokenRepository.getClientTokenIntent(),
                            clientTokenRepository.getStatusUrl().orEmpty(),
                            PaymentMethodType.safeValueOf(
                                paymentMethodRepository.getPaymentMethod()
                                    .paymentInstrumentData?.paymentMethodType
                            )
                        )
                    )
                )
            }
            else -> eventDispatcher.dispatchEvents(
                listOf(
                    CheckoutEvent.PaymentMethodPresented,
                    CheckoutEvent.StartAsyncRedirectFlow(
                        paymentMethodRepository.getPaymentMethod().paymentInstrumentData
                            ?.paymentMethodType?.split("_")?.last().orEmpty(),
                        PaymentMethodType.safeValueOf(
                            paymentMethodRepository.getPaymentMethod()
                                .paymentInstrumentData?.paymentMethodType
                        ),
                        clientTokenRepository.getRedirectUrl().orEmpty(),
                        clientTokenRepository.getStatusUrl().orEmpty(),
                    )
                )
            )
        }
    }
}
