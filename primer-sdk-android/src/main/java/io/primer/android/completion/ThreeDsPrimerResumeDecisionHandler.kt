package io.primer.android.completion

import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.models.ThreeDsFailureContextParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperFactory
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.exception.ThreeDsLibraryNotFoundException
import io.primer.android.domain.exception.ThreeDsLibraryVersionMismatchException
import io.primer.android.domain.mock.repository.MockConfigurationRepository
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.domain.payments.helpers.ResumeEventResolver
import io.primer.android.domain.payments.methods.repository.PaymentMethodDescriptorsRepository
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.domain.token.repository.ValidateTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.payment.processor3ds.Processor3DS
import io.primer.android.threeds.BuildConfig
import io.primer.android.threeds.domain.interactor.DefaultThreeDsInteractor
import io.primer.android.threeds.domain.models.FailureThreeDsContinueAuthParams
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.threeds.domain.respository.ThreeDsRepository
import io.primer.android.threeds.helpers.ThreeDsLibraryVersionValidator
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

internal class ThreeDsPrimerResumeDecisionHandler(
    validationTokenRepository: ValidateTokenRepository,
    private val clientTokenRepository: ClientTokenRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    paymentResultRepository: PaymentResultRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val mockConfigurationRepository: MockConfigurationRepository,
    private val threeDsSdkClassValidator: ThreeDsSdkClassValidator,
    private val threeDsLibraryVersionValidator: ThreeDsLibraryVersionValidator,
    private val errorEventResolver: BaseErrorEventResolver,
    private val eventDispatcher: EventDispatcher,
    private val threeDsRepository: ThreeDsRepository,
    private val errorMapperFactory: ErrorMapperFactory,
    private val resumeHandlerFactory: ResumeHandlerFactory,
    private val logReporter: LogReporter,
    private val config: PrimerConfig,
    paymentMethodDescriptorsRepository: PaymentMethodDescriptorsRepository,
    retailerOutletRepository: RetailOutletRepository,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DefaultPrimerResumeDecisionHandler(
    validationTokenRepository,
    clientTokenRepository,
    paymentMethodRepository,
    paymentResultRepository,
    analyticsRepository,
    errorEventResolver,
    eventDispatcher,
    logReporter,
    config,
    paymentMethodDescriptorsRepository,
    retailerOutletRepository,
    coroutineDispatcher
) {

    override fun handleClientToken(clientToken: String) {
        super.handleClientToken(clientToken)
        when {
            clientTokenRepository.getClientTokenIntent() == ClientTokenIntent.PROCESSOR_3DS.name
            -> when (
                config.settings.fromHUC &&
                    paymentMethodRepository.getPaymentMethod().isVaulted
            ) {
                true ->
                    eventDispatcher.dispatchEvent(
                        CheckoutEvent.Start3DSVault(
                            Processor3DS(
                                clientTokenRepository.getRedirectUrl().orEmpty(),
                                clientTokenRepository.getStatusUrl().orEmpty()
                            )
                        )
                    )

                false -> eventDispatcher.dispatchEvent(
                    CheckoutEvent.Start3DS(
                        Processor3DS(
                            clientTokenRepository.getRedirectUrl().orEmpty(),
                            clientTokenRepository.getStatusUrl().orEmpty()
                        )
                    )
                )
            }

            threeDsSdkClassValidator.is3dsSdkIncluded().not() ->
                continueAuthWithException(
                    ThreeDsLibraryNotFoundException(
                        ThreeDsSdkClassValidator.THREE_DS_CLASS_NOT_LOADED_ERROR
                    )
                )

            threeDsLibraryVersionValidator.isValidVersion().not() -> {
                continueAuthWithException(
                    ThreeDsLibraryVersionMismatchException(
                        BuildConfig.SDK_VERSION_STRING,
                        ThreeDsFailureContextParams(null, null)
                    )
                )
            }

            else -> when (mockConfigurationRepository.isMockedFlow()) {
                true -> eventDispatcher.dispatchEvent(CheckoutEvent.Start3DSMock)
                false -> {
                    when (
                        config.settings.fromHUC &&
                            paymentMethodRepository.getPaymentMethod().isVaulted
                    ) {
                        true -> eventDispatcher.dispatchEvent(CheckoutEvent.Start3DSVault())
                        false -> eventDispatcher.dispatchEvent(CheckoutEvent.Start3DS())
                    }
                }
            }
        }
    }

    private fun continueAuthWithException(throwable: Throwable) {
        CoroutineScope(coroutineDispatcher).launch {
            threeDsRepository.continue3DSAuth(
                paymentMethodRepository.getPaymentMethod().token,
                FailureThreeDsContinueAuthParams(
                    error = errorMapperFactory.buildErrorMapper(
                        ErrorMapperType.THREE_DS
                    ).getPrimerError(throwable).also { error ->
                        logAnalytics(error)
                        logReporter.warn(
                            "${error.description} ${error.recoverySuggestion.orEmpty()}"
                        )
                    }
                )
            ).catch {
                errorEventResolver.resolve(
                    it,
                    ErrorMapperType.THREE_DS
                )
            }.collect {
                ResumeEventResolver(config, resumeHandlerFactory, eventDispatcher).resolve(
                    paymentMethodRepository.getPaymentMethod().paymentInstrumentType,
                    paymentMethodRepository.getPaymentMethod().isVaulted,
                    it.resumeToken
                )
            }
        }
    }

    private fun logAnalytics(error: PrimerError) = analyticsRepository.addEvent(
        MessageAnalyticsParams(
            MessageType.ERROR,
            "${DefaultThreeDsInteractor.ANALYTICS_3DS_COMPONENT}: ${error.description}",
            Severity.ERROR,
            error.diagnosticsId,
            error.context
        )
    )
}
