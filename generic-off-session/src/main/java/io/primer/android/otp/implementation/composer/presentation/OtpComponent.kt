package io.primer.android.otp.implementation.composer.presentation

import androidx.annotation.VisibleForTesting
import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.utils.RawDataManagerAnalyticsConstants
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadata
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadataState
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.core.extensions.flatMap
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.errors.extensions.onFailureWithCancellation
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.otp.PrimerOtpData
import io.primer.android.otp.implementation.errors.data.exception.OtpIllegalValueKey
import io.primer.android.otp.implementation.payment.delegate.OtpPaymentDelegate
import io.primer.android.otp.implementation.tokenization.presentation.OtpTokenizationDelegate
import io.primer.android.otp.implementation.tokenization.presentation.composable.OtpTokenizationInputable
import io.primer.android.paymentmethods.CollectableDataValidator
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.core.composer.RawDataPaymentMethodComponent
import io.primer.android.payments.core.helpers.PollingStartHandler
import io.primer.android.payments.core.status.domain.AsyncPaymentMethodPollingInteractor
import io.primer.android.payments.core.status.domain.model.AsyncStatusParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

internal class OtpComponent(
    private val tokenizationDelegate: OtpTokenizationDelegate,
    private val pollingInteractor: AsyncPaymentMethodPollingInteractor,
    private val paymentDelegate: OtpPaymentDelegate,
    private val pollingStartHandler: PollingStartHandler,
    private val collectableDataValidator: CollectableDataValidator<PrimerOtpData>,
    private val errorMapperRegistry: ErrorMapperRegistry,
    private val sdkAnalyticsEventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate,
) : RawDataPaymentMethodComponent<PrimerOtpData>() {
    private var primerSessionIntent by Delegates.notNull<PrimerSessionIntent>()
    private var paymentMethodType by Delegates.notNull<String>()

    private val _componentInputValidations = MutableSharedFlow<List<PrimerInputValidationError>>()
    override val componentInputValidations: Flow<List<PrimerInputValidationError>> = _componentInputValidations

    override val metadataFlow: Flow<PrimerPaymentMethodMetadata> = emptyFlow()
    override val metadataStateFlow: Flow<PrimerPaymentMethodMetadataState> = emptyFlow()

    private val collectedData: MutableSharedFlow<PrimerOtpData> = MutableSharedFlow(replay = 1)

    override fun start(
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
    ) {
        this.paymentMethodType = paymentMethodType
        this.primerSessionIntent = sessionIntent
        composerScope.launch {
            pollingStartHandler.startPolling.collectLatest { pollingStartData ->
                startPolling(
                    url = pollingStartData.statusUrl,
                    paymentMethodType = pollingStartData.paymentMethodType,
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun startPolling(
        url: String,
        paymentMethodType: String,
    ) = composerScope.launch {
        runCatching {
            pollingInteractor.execute(
                AsyncStatusParams(url, paymentMethodType),
            ).mapLatest { status ->
                paymentDelegate.resumePayment(status.resumeToken)
            }.catch {
                paymentDelegate.handleError(it)
            }.collect()
        }.onFailureWithCancellation(paymentMethodType, paymentDelegate::handleError)
    }

    override fun updateCollectedData(collectedData: PrimerOtpData) {
        logSdkAnalyticsEvent(
            methodName = RawDataManagerAnalyticsConstants.SET_RAW_DATA_METHOD,
            paymentMethodType = paymentMethodType,
            context =
                mapOf(
                    RawDataManagerAnalyticsConstants.PAYMENT_METHOD_TYPE_PARAM to paymentMethodType,
                ).filterValues { it.isNotBlank() },
        )
        composerScope.launch {
            this@OtpComponent.collectedData.emit(collectedData)
        }

        validateRawData(collectedData)
    }

    override fun submit() {
        tokenize()
    }

    private fun tokenize() =
        composerScope.launch {
            runCatching {
                requireNotNullCheck(collectedData.replayCache.last(), OtpIllegalValueKey.OTP_DATA)
            }
                .flatMap {
                    tokenizationDelegate.tokenize(
                        OtpTokenizationInputable(
                            otpData = it,
                            paymentMethodType = paymentMethodType,
                            primerSessionIntent = primerSessionIntent,
                        ),
                    )
                }
                .flatMap { paymentMethodTokenData ->
                    paymentDelegate.handlePaymentMethodToken(
                        paymentMethodTokenData = paymentMethodTokenData,
                        primerSessionIntent = primerSessionIntent,
                    )
                }.onFailure {
                    paymentDelegate.handleError(it)
                }
        }

    private fun validateRawData(otp: PrimerOtpData) =
        composerScope.launch {
            runSuspendCatching {
                collectableDataValidator.validate(otp)
                    .onSuccess { errors ->
                        val inputErrors =
                            errors.map {
                                PrimerInputValidationError(
                                    errorId = it.errorId,
                                    description = it.description,
                                    inputElementType = PrimerInputElementType.OTP_CODE,
                                )
                            }
                        _componentInputValidations.emit(inputErrors)
                    }.onFailure { throwable ->
                        with(errorMapperRegistry.getPrimerError(throwable)) {
                            _componentInputValidations.emit(
                                listOf(
                                    PrimerInputValidationError(
                                        errorId = errorId,
                                        description = description,
                                        inputElementType = PrimerInputElementType.OTP_CODE,
                                    ),
                                ),
                            )
                        }
                    }
            }
        }

    private fun logSdkAnalyticsEvent(
        methodName: String,
        paymentMethodType: String,
        context: Map<String, String> = emptyMap(),
    ) = composerScope.launch {
        sdkAnalyticsEventLoggingDelegate.logSdkAnalyticsEvent(
            methodName = methodName,
            paymentMethodType = paymentMethodType,
            context = context,
        )
    }
}
