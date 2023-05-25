package io.primer.android.completion

import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.deeplink.async.repository.AsyncPaymentMethodDeeplinkRepository
import io.primer.android.domain.error.ErrorMapperFactory
import io.primer.android.domain.mock.repository.MockConfigurationRepository
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.domain.payments.methods.repository.PaymentMethodDescriptorsRepository
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.domain.token.repository.ValidateTokenRepository
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.threeds.domain.respository.ThreeDsRepository
import io.primer.android.threeds.helpers.ThreeDsLibraryVersionValidator
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator

internal class ResumeHandlerFactory(
    private val validationTokenRepository: ValidateTokenRepository,
    private val clientTokenRepository: ClientTokenRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val paymentResultRepository: PaymentResultRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val mockConfigurationRepository: MockConfigurationRepository,
    private val threeDsSdkClassValidator: ThreeDsSdkClassValidator,
    private val threeDsLibraryVersionValidator: ThreeDsLibraryVersionValidator,
    private val threeDsRepository: ThreeDsRepository,
    private val errorMapperFactory: ErrorMapperFactory,
    private val errorEventResolver: BaseErrorEventResolver,
    private val eventDispatcher: EventDispatcher,
    private val logger: Logger,
    private val config: PrimerConfig,
    private val paymentMethodDescriptorsRepository: PaymentMethodDescriptorsRepository,
    private val retailerOutletRepository: RetailOutletRepository,
    private val asyncPaymentMethodDeeplinkRepository: AsyncPaymentMethodDeeplinkRepository,
) {

    fun getResumeHandler(
        paymentInstrumentType: String,
    ): PrimerResumeDecisionHandler {
        return paymentMethodDescriptorsRepository.getPaymentMethodDescriptors()
            .firstOrNull { descriptor
                ->
                descriptor.config.type ==
                    paymentMethodRepository.getPaymentMethod().paymentMethodType
            }
            ?.resumeHandler
            ?: when (paymentInstrumentType) {
                CARD_INSTRUMENT_TYPE,
                PaymentMethodType.GOOGLE_PAY.name -> ThreeDsPrimerResumeDecisionHandler(
                    validationTokenRepository,
                    clientTokenRepository,
                    paymentMethodRepository,
                    paymentResultRepository,
                    analyticsRepository,
                    mockConfigurationRepository,
                    threeDsSdkClassValidator,
                    threeDsLibraryVersionValidator,
                    errorEventResolver,
                    eventDispatcher,
                    threeDsRepository,
                    errorMapperFactory,
                    this,
                    logger,
                    config,
                    paymentMethodDescriptorsRepository,
                    retailerOutletRepository
                )
                ASYNC_PAYMENT_METHOD,
                CARD_ASYNC_PAYMENT_METHOD -> AsyncPaymentPrimerResumeDecisionHandler(
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
                    asyncPaymentMethodDeeplinkRepository,
                )
                else -> DefaultPrimerResumeDecisionHandler(
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
                    retailerOutletRepository
                )
            }
    }

    private companion object {

        const val CARD_INSTRUMENT_TYPE = "PAYMENT_CARD"
        const val ASYNC_PAYMENT_METHOD = "OFF_SESSION_PAYMENT"
        const val CARD_ASYNC_PAYMENT_METHOD = "CARD_OFF_SESSION_PAYMENT"
    }
}
