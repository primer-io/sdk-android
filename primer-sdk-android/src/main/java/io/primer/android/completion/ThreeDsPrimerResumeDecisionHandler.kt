package io.primer.android.completion

import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.exception.ThreeDsLibraryNotFoundException
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.domain.payments.methods.repository.PaymentMethodsRepository
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.domain.token.repository.ValidateTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.payment.processor3ds.Processor3DS
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class ThreeDsPrimerResumeDecisionHandler(
    validationTokenRepository: ValidateTokenRepository,
    private val clientTokenRepository: ClientTokenRepository,
    paymentMethodRepository: PaymentMethodRepository,
    paymentResultRepository: PaymentResultRepository,
    analyticsRepository: AnalyticsRepository,
    private val threeDsSdkClassValidator: ThreeDsSdkClassValidator,
    private val errorEventResolver: BaseErrorEventResolver,
    private val eventDispatcher: EventDispatcher,
    logger: Logger,
    config: PrimerConfig,
    paymentMethodsRepository: PaymentMethodsRepository,
    retailerOutletRepository: RetailOutletRepository,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
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
    paymentMethodsRepository,
    retailerOutletRepository,
    coroutineDispatcher
) {

    override fun handleClientToken(clientToken: String) {
        super.handleClientToken(clientToken)
        when {
            clientTokenRepository.getClientTokenIntent() == ClientTokenIntent.PROCESSOR_3DS.name
            -> {
                eventDispatcher.dispatchEvent(
                    CheckoutEvent.Start3DS(
                        Processor3DS(
                            clientTokenRepository.getRedirectUrl().orEmpty(),
                            clientTokenRepository.getStatusUrl().orEmpty()
                        )
                    )
                )
            }
            threeDsSdkClassValidator.is3dsSdkIncluded() -> {
                eventDispatcher.dispatchEvent(CheckoutEvent.Start3DS())
            }
            else -> errorEventResolver.resolve(
                ThreeDsLibraryNotFoundException(
                    ThreeDsSdkClassValidator.THREE_DS_CLASS_NOT_LOADED_ERROR
                ),
                ErrorMapperType.PAYMENT_RESUME
            )
        }
    }
}
