package io.primer.android.completion

import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.exception.ThreeDsLibraryNotFoundException
import io.primer.android.domain.token.repository.ValidateTokenRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class ThreeDsPrimerResumeDecisionHandler(
    validationTokenRepository: ValidateTokenRepository,
    clientTokenRepository: ClientTokenRepository,
    paymentMethodRepository: PaymentMethodRepository,
    analyticsRepository: AnalyticsRepository,
    private val threeDsSdkClassValidator: ThreeDsSdkClassValidator,
    private val errorEventResolver: BaseErrorEventResolver,
    private val eventDispatcher: EventDispatcher,
    logger: Logger,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DefaultPrimerResumeDecisionHandler(
    validationTokenRepository,
    clientTokenRepository,
    paymentMethodRepository,
    analyticsRepository,
    errorEventResolver,
    eventDispatcher,
    logger,
    coroutineDispatcher
) {

    override fun handleClientToken(clientToken: String) {
        super.handleClientToken(clientToken)
        if (threeDsSdkClassValidator.is3dsSdkIncluded()) {
            eventDispatcher.dispatchEvent(
                CheckoutEvent.Start3DS
            )
        } else errorEventResolver.resolve(
            ThreeDsLibraryNotFoundException(
                ThreeDsSdkClassValidator.THREE_DS_CLASS_NOT_LOADED_ERROR
            ),
            ErrorMapperType.PAYMENT_RESUME
        )
    }
}
