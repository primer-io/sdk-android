package io.primer.android.card.implementation.composer

import android.app.Activity
import android.content.Intent
import io.primer.android.analytics.utils.RawDataManagerAnalyticsConstants
import io.primer.android.PrimerSessionIntent
import io.primer.android.card.implementation.composer.ui.navigation.CardNative3DSActivityLauncherParams
import io.primer.android.card.implementation.composer.ui.navigation.CardProcessor3DSActivityLauncherParams
import io.primer.android.card.implementation.composer.ui.navigation.MockCard3DSActivityLauncherParams
import io.primer.android.card.implementation.payment.delegate.CardPaymentDelegate
import io.primer.android.card.implementation.payment.delegate.ProcessorThreeDsInitialLauncherParams
import io.primer.android.card.implementation.payment.delegate.ThreeDsInitialLauncherParams
import io.primer.android.card.implementation.tokenization.presentation.CardTokenizationDelegate
import io.primer.android.card.implementation.tokenization.presentation.composable.CardTokenizationInputable
import io.primer.android.configuration.mock.presentation.MockConfigurationDelegate
import io.primer.android.core.extensions.debounce
import io.primer.android.core.extensions.flatMap
import io.primer.android.core.extensions.getSerializableCompat
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.paymentmethods.PaymentInputDataValidator
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.core.composer.RawDataPaymentMethodComponent
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.paymentmethods.core.composer.composable.UiEventable
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadata
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadataState
import io.primer.android.processor3ds.ui.Processor3dsWebViewActivity
import io.primer.android.threeds.ui.ThreeDsActivity
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.cardShared.binData.domain.CardDataMetadataRetriever
import io.primer.cardShared.binData.domain.CardMetadataStateRetriever
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityResultIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityStartIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import io.primer.paymentMethodCoreUi.core.ui.webview.WebViewActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

internal class CardComponent(
    private val tokenizationDelegate: CardTokenizationDelegate,
    private val paymentDelegate: CardPaymentDelegate,
    private val cardDataMetadataRetriever: CardDataMetadataRetriever,
    private val cardDataMetadataStateRetriever: CardMetadataStateRetriever,
    private val cardInputDataValidator: PaymentInputDataValidator<PrimerCardData>,
    private val sdkAnalyticsEventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate,
    private val mockConfigurationDelegate: MockConfigurationDelegate
) : RawDataPaymentMethodComponent<PrimerCardData>(),
    ActivityResultIntentHandler,
    ActivityStartIntentHandler,
    UiEventable {

    private var primerSessionIntent by Delegates.notNull<PrimerSessionIntent>()
    private var paymentMethodType by Delegates.notNull<String>()

    private val _uiEvent = MutableSharedFlow<ComposerUiEvent>()
    override val uiEvent: SharedFlow<ComposerUiEvent> = _uiEvent

    private val _componentInputValidations =
        MutableSharedFlow<List<PrimerInputValidationError>>()
    override val componentInputValidations: Flow<List<PrimerInputValidationError>> = _componentInputValidations

    private val _metadataFlow =
        MutableSharedFlow<PrimerPaymentMethodMetadata>()
    override val metadataFlow: Flow<PrimerPaymentMethodMetadata> = _metadataFlow.distinctUntilChanged()

    override val metadataStateFlow: Flow<PrimerPaymentMethodMetadataState>
        get() = cardDataMetadataStateRetriever.metadataState

    private val _collectedData: MutableSharedFlow<PrimerCardData> = MutableSharedFlow(replay = 1)

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

    override fun updateCollectedData(collectedData: PrimerCardData) {
        val oldCardCollectableData = _collectedData.replayCache.lastOrNull()
        val cardDataChanged =
            oldCardCollectableData?.copy(cardNetwork = null) != collectedData.copy(cardNetwork = null)
        logSdkAnalyticsEvent(
            methodName = RawDataManagerAnalyticsConstants.SET_RAW_DATA_METHOD,
            paymentMethodType = paymentMethodType,
            context = mapOf(
                RawDataManagerAnalyticsConstants.PAYMENT_METHOD_TYPE_PARAM to paymentMethodType,
                RawDataManagerAnalyticsConstants.PREFERRED_NETWORK_PARAM to collectedData.cardNetwork?.name.orEmpty()
            ).filterValues { it.isNotBlank() }
        )
        composerScope.launch {
            _collectedData.emit(collectedData)
        }
        // don't trigger side effects in case only card network changed
        if (cardDataChanged) {
            metadataRawDataDataUpdated(collectedData)
            // validate card data each time it changes
            validateRawData(collectedData)

            composerScope.launch {
                _metadataFlow.emit(cardDataMetadataRetriever.retrieveMetadata(collectedData))
            }
        }
    }

    override fun submit() {
        composerScope.launch {
            startTokenization(_collectedData.replayCache.last())
        }
    }

    override fun handleActivityResultIntent(params: PaymentMethodLauncherParams, resultCode: Int, intent: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                when (params.initialLauncherParams) {
                    is ThreeDsInitialLauncherParams -> composerScope.launch {
                        paymentDelegate.resumePayment(
                            intent?.getStringExtra(ThreeDsActivity.RESUME_TOKEN_EXTRA_KEY).orEmpty()
                        )
                    }

                    is ProcessorThreeDsInitialLauncherParams -> composerScope.launch {
                        paymentDelegate.resumePayment(
                            intent?.getStringExtra(Processor3dsWebViewActivity.RESUME_TOKEN_EXTRA_KEY).orEmpty()
                        )
                    }

                    null -> Unit
                }
            }

            Activity.RESULT_CANCELED -> {
                composerScope.launch {
                    paymentDelegate.handleError(
                        PaymentMethodCancelledException(
                            paymentMethodType = paymentMethodType
                        )
                    )
                }
            }

            WebViewActivity.RESULT_ERROR -> {
                composerScope.launch {
                    intent?.getSerializableCompat<Exception>(Processor3dsWebViewActivity.ERROR_KEY)?.let {
                        paymentDelegate.handleError(it)
                    }
                }
            }
        }

        closeProxyScreen()
    }

    override fun handleActivityStartEvent(params: PaymentMethodLauncherParams) {
        composerScope.launch {
            when (val initialLaunchEvents = params.initialLauncherParams) {
                is ThreeDsInitialLauncherParams -> {
                    _uiEvent.emit(
                        if (mockConfigurationDelegate.isMockedFlow()) {
                            ComposerUiEvent.Navigate(
                                MockCard3DSActivityLauncherParams(
                                    paymentMethodType = paymentMethodType
                                )
                            )
                        } else {
                            ComposerUiEvent.Navigate(
                                CardNative3DSActivityLauncherParams(
                                    paymentMethodType = paymentMethodType,
                                    supportedThreeDsVersions = initialLaunchEvents.supportedThreeDsProtocolVersions
                                )
                            )
                        }
                    )
                }
                is ProcessorThreeDsInitialLauncherParams -> _uiEvent.emit(
                    ComposerUiEvent.Navigate(
                        CardProcessor3DSActivityLauncherParams(
                            paymentMethodType = paymentMethodType,
                            redirectUrl = initialLaunchEvents.processor3DS.redirectUrl,
                            statusUrl = initialLaunchEvents.processor3DS.statusUrl,
                            title = initialLaunchEvents.processor3DS.title
                        )
                    )
                )

                else -> Unit
            }
        }
    }

    private fun startTokenization(
        cardData: PrimerCardData
    ) = composerScope.launch {
        tokenizationDelegate.tokenize(
            CardTokenizationInputable(
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

    private fun closeProxyScreen() = composerScope.launch {
        _uiEvent.emit(ComposerUiEvent.Finish)
    }

    private val metadataRawDataDataUpdated: (PrimerCardData) -> Unit =
        composerScope.debounce { rawData ->
            cardDataMetadataStateRetriever.handleInputData(rawData)
        }

    private fun validateRawData(
        cardData: PrimerCardData
    ) = composerScope.launch {
        runSuspendCatching {
            _componentInputValidations.emit(cardInputDataValidator.validate(cardData))
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
