package io.primer.android.bancontact.implementation.composer

import androidx.annotation.VisibleForTesting
import io.primer.android.analytics.utils.RawDataManagerAnalyticsConstants
import io.primer.android.PrimerSessionIntent
import io.primer.android.bancontact.PrimerBancontactCardData
import io.primer.android.bancontact.implementation.metadata.domain.BancontactCardDataMetadataRetriever
import io.primer.android.bancontact.implementation.payment.delegate.AdyenBancontactPaymentDelegate
import io.primer.android.bancontact.implementation.tokenization.presentation.AdyenBancontactTokenizationDelegate
import io.primer.android.bancontact.implementation.tokenization.presentation.composable.AdyenBancontactTokenizationInputable
import io.primer.android.core.extensions.flatMap
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.paymentmethods.PaymentInputDataValidator
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.core.composer.RawDataPaymentMethodComponent
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadata
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadataState
import io.primer.android.payments.core.status.domain.AsyncPaymentMethodPollingInteractor
import io.primer.android.payments.core.status.domain.model.AsyncStatusParams
import io.primer.android.webRedirectShared.implementation.composer.presentation.BaseWebRedirectComposer
import io.primer.android.webRedirectShared.implementation.composer.presentation.WebRedirectLauncherParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

internal class AdyenBancontactComponent(
    private val tokenizationDelegate: AdyenBancontactTokenizationDelegate,
    private val pollingInteractor: AsyncPaymentMethodPollingInteractor,
    private val paymentDelegate: AdyenBancontactPaymentDelegate,
    private val cardInputDataValidator: PaymentInputDataValidator<PrimerBancontactCardData>,
    private val metadataRetriever: BancontactCardDataMetadataRetriever,
    private val sdkAnalyticsEventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate
) : RawDataPaymentMethodComponent<PrimerBancontactCardData>(),
    BaseWebRedirectComposer {

    override val scope: CoroutineScope = composerScope

    override val _uiEvent: MutableSharedFlow<ComposerUiEvent> = MutableSharedFlow()

    private val _metadataFlow =
        MutableSharedFlow<PrimerPaymentMethodMetadata>()
    override val metadataFlow: Flow<PrimerPaymentMethodMetadata> = _metadataFlow.distinctUntilChanged()

    override val metadataStateFlow: Flow<PrimerPaymentMethodMetadataState> = emptyFlow()

    private var primerSessionIntent by Delegates.notNull<PrimerSessionIntent>()
    private var paymentMethodType by Delegates.notNull<String>()

    private val _componentInputValidations =
        MutableSharedFlow<List<PrimerInputValidationError>>()
    override val componentInputValidations: Flow<List<PrimerInputValidationError>> = _componentInputValidations

    private val _collectedData: MutableSharedFlow<PrimerBancontactCardData> = MutableSharedFlow(replay = 1)

    override fun start(paymentMethodType: String, sessionIntent: PrimerSessionIntent) {
        this.paymentMethodType = paymentMethodType
        this.primerSessionIntent = sessionIntent
        composerScope.launch {
            launch {
                paymentDelegate.uiEvent.collect {
                    _uiEvent.emit(it)
                }
            }
        }
    }

    override fun onResultCancelled(params: WebRedirectLauncherParams) {
        scope.launch {
            paymentDelegate.handleError(
                throwable = PaymentMethodCancelledException(paymentMethodType = params.paymentMethodType)
            )
        }
    }

    override fun onResultOk(params: WebRedirectLauncherParams) {
        startPolling(statusUrl = params.statusUrl, paymentMethodType = params.paymentMethodType)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun startPolling(statusUrl: String, paymentMethodType: String) =
        composerScope.launch {
            pollingInteractor.execute(
                AsyncStatusParams(statusUrl, paymentMethodType)
            ).mapLatest { status ->
                paymentDelegate.resumePayment(status.resumeToken)
            }.catch {
                paymentDelegate.handleError(it)
            }.collect()
        }

    override fun updateCollectedData(collectedData: PrimerBancontactCardData) {
        logSdkAnalyticsEvent(
            methodName = RawDataManagerAnalyticsConstants.SET_RAW_DATA_METHOD,
            paymentMethodType = paymentMethodType,
            context = mapOf(
                RawDataManagerAnalyticsConstants.PAYMENT_METHOD_TYPE_PARAM to paymentMethodType
            ).filterValues { it.isNotBlank() }
        )
        composerScope.launch {
            _collectedData.emit(collectedData)
        }

        composerScope.launch {
            _metadataFlow.emit(metadataRetriever.retrieveMetadata(collectedData))
        }

        validateRawData(collectedData)
    }

    override fun submit() {
        startTokenization(_collectedData.replayCache.last())
    }

    private fun startTokenization(
        cardData: PrimerBancontactCardData
    ) = composerScope.launch {
        tokenizationDelegate.tokenize(
            AdyenBancontactTokenizationInputable(
                cardData = cardData,
                paymentMethodType = paymentMethodType,
                primerSessionIntent = primerSessionIntent
            )
        ).flatMap { paymentMethodTokenData ->
            paymentDelegate.handlePaymentMethodToken(
                paymentMethodTokenData = paymentMethodTokenData,
                primerSessionIntent = primerSessionIntent
            )
        }.onFailure {
            paymentDelegate.handleError(it)
        }
    }

    private fun validateRawData(
        bancontactCardData: PrimerBancontactCardData
    ) = composerScope.launch {
        runSuspendCatching {
            _componentInputValidations.emit(cardInputDataValidator.validate(bancontactCardData))
        }
    }

    private fun logSdkAnalyticsEvent(
        methodName: String,
        paymentMethodType: String,
        context: Map<String, String> = emptyMap()
    ) = composerScope.launch {
        sdkAnalyticsEventLoggingDelegate.logSdkAnalyticsEvent(
            methodName = methodName,
            paymentMethodType = paymentMethodType,
            context = context
        )
    }
}
