package io.primer.android.googlepay.implementation.composer

import android.app.Activity
import android.content.Intent
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import io.primer.android.PrimerSessionIntent
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.configuration.mock.presentation.MockConfigurationDelegate
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.extensions.flatMap
import io.primer.android.core.extensions.getSerializableCompat
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.googlepay.implementation.clientSessionActions.presentation.mapper.mapToMultipleActionUpdateParams
import io.primer.android.googlepay.implementation.clientSessionActions.presentation.mapper.mapToShippingOptionIdParams
import io.primer.android.googlepay.implementation.composer.ui.navigation.GooglePayNative3DSActivityLauncherParams
import io.primer.android.googlepay.implementation.composer.ui.navigation.GooglePayProcessor3DSActivityLauncherParams
import io.primer.android.googlepay.implementation.composer.ui.navigation.MockGooglePay3DSActivityLauncherParams
import io.primer.android.googlepay.implementation.errors.domain.exception.GooglePayException
import io.primer.android.googlepay.implementation.payment.delegate.GooglePayPaymentDelegate
import io.primer.android.googlepay.implementation.payment.delegate.ProcessorThreeDsInitialLauncherParams
import io.primer.android.googlepay.implementation.payment.delegate.ThreeDsInitialLauncherParams
import io.primer.android.googlepay.implementation.tokenization.presentation.GooglePayTokenizationCollectorDelegate
import io.primer.android.googlepay.implementation.tokenization.presentation.GooglePayTokenizationDelegate
import io.primer.android.googlepay.implementation.tokenization.presentation.composable.GooglePayTokenizationInputable
import io.primer.android.googlepay.implementation.validation.GooglePayShippingMethodUpdateValidator
import io.primer.android.googlepay.implementation.validation.GooglePayValidationRulesResolver
import io.primer.android.paymentmethods.core.composer.InternalNativeUiPaymentMethodComponent
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.payments.core.tokenization.presentation.composable.NoOpPaymentMethodTokenizationCollectorParams
import io.primer.android.processor3ds.ui.Processor3dsWebViewActivity
import io.primer.android.threeds.ui.ThreeDsActivity
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityResultIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityStartIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import io.primer.paymentMethodCoreUi.core.ui.webview.WebViewActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

internal class GooglePayComponent(
    private val tokenizationCollectorDelegate: GooglePayTokenizationCollectorDelegate,
    private val tokenizationDelegate: GooglePayTokenizationDelegate,
    private val shippingMethodUpdateValidator: GooglePayShippingMethodUpdateValidator,
    private val actionInteractor: ActionInteractor,
    private val validationRulesResolver: GooglePayValidationRulesResolver,
    private val paymentDelegate: GooglePayPaymentDelegate,
    private val mockConfigurationDelegate: MockConfigurationDelegate,
) : InternalNativeUiPaymentMethodComponent(),
    ActivityResultIntentHandler,
    ActivityStartIntentHandler {
    override fun start(
        paymentMethodType: String,
        primerSessionIntent: PrimerSessionIntent,
    ) {
        this.paymentMethodType = paymentMethodType
        this.primerSessionIntent = primerSessionIntent
        composerScope.launch {
            launch {
                tokenizationCollectorDelegate.uiEvent.collect {
                    _uiEvent.emit(it)
                }
            }
            launch {
                paymentDelegate.uiEvent.collect {
                    _uiEvent.emit(it)
                }
            }
        }

        composerScope.launch {
            _uiEvent.emit(
                ComposerUiEvent.Navigate(
                    PaymentMethodLauncherParams(
                        paymentMethodType = paymentMethodType,
                        sessionIntent = primerSessionIntent,
                    ),
                ),
            )
        }
    }

    override fun handleActivityResultIntent(
        params: PaymentMethodLauncherParams,
        resultCode: Int,
        intent: Intent?,
    ) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                when (params.initialLauncherParams) {
                    is ThreeDsInitialLauncherParams ->
                        composerScope.launch {
                            paymentDelegate.resumePayment(
                                intent?.getStringExtra(ThreeDsActivity.RESUME_TOKEN_EXTRA_KEY).orEmpty(),
                            )
                        }

                    is ProcessorThreeDsInitialLauncherParams ->
                        composerScope.launch {
                            paymentDelegate.resumePayment(
                                intent?.getStringExtra(Processor3dsWebViewActivity.RESUME_TOKEN_EXTRA_KEY).orEmpty(),
                            )
                        }

                    null -> handlePaymentData(intent?.let { PaymentData.getFromIntent(it) })
                }
            }

            Activity.RESULT_CANCELED -> {
                composerScope.launch {
                    paymentDelegate.handleError(
                        PaymentMethodCancelledException(
                            paymentMethodType,
                        ),
                    )
                }
            }

            AutoResolveHelper.RESULT_ERROR -> {
                composerScope.launch {
                    AutoResolveHelper.getStatusFromIntent(intent)?.let { status ->
                        paymentDelegate.handleError(GooglePayException(status))
                    }
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
                                MockGooglePay3DSActivityLauncherParams(
                                    paymentMethodType = paymentMethodType,
                                ),
                            )
                        } else {
                            ComposerUiEvent.Navigate(
                                GooglePayNative3DSActivityLauncherParams(
                                    paymentMethodType = paymentMethodType,
                                    supportedThreeDsVersions = initialLaunchEvents.supportedThreeDsProtocolVersions,
                                ),
                            )
                        },
                    )
                }

                is ProcessorThreeDsInitialLauncherParams ->
                    _uiEvent.emit(
                        ComposerUiEvent.Navigate(
                            GooglePayProcessor3DSActivityLauncherParams(
                                paymentMethodType = paymentMethodType,
                                redirectUrl = initialLaunchEvents.processor3DS.redirectUrl,
                                statusUrl = initialLaunchEvents.processor3DS.statusUrl,
                                title = initialLaunchEvents.processor3DS.title,
                            ),
                        ),
                    )

                else ->
                    tokenizationCollectorDelegate.startDataCollection(
                        params = NoOpPaymentMethodTokenizationCollectorParams,
                    )
            }
        }
    }

    private fun startTokenization(paymentData: PaymentData) =
        composerScope.launch {
            try {
                tokenizationDelegate.tokenize(
                    GooglePayTokenizationInputable(
                        paymentData = paymentData,
                        paymentMethodType = paymentMethodType,
                        primerSessionIntent = primerSessionIntent,
                    ),
                ).flatMap { paymentMethodTokenData ->
                    paymentDelegate.handlePaymentMethodToken(
                        paymentMethodTokenData = paymentMethodTokenData,
                        primerSessionIntent = primerSessionIntent,
                    )
                }.onFailure {
                    paymentDelegate.handleError(it)
                }
            } catch (ignored: CancellationException) {
                paymentDelegate.handleError(PaymentMethodCancelledException(paymentMethodType = paymentMethodType))
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun handlePaymentData(paymentData: PaymentData?) =
        composerScope.launch {
            combine(
                validationRulesResolver.resolve().rules.map {
                    flowOf(it.validate(paymentData))
                },
            ) { validationResults ->
                validationResults.forEach { result ->
                    if (result is ValidationResult.Failure) throw result.exception
                }
            }.mapLatest {
                handlePaymentDataUpdate(paymentData).flatMap {
                    handleShippingMethodIdUpdate(paymentData).map {
                        startTokenization(requireNotNull(paymentData))
                    }
                }
            }.catch {
                paymentDelegate.handleError(it)
            }.collect()
        }

    private suspend fun handlePaymentDataUpdate(paymentData: PaymentData?): Result<Unit> {
        val action = paymentData.mapToMultipleActionUpdateParams()
        return action?.let {
            actionInteractor(action).map { }
        } ?: Result.success(Unit)
    }

    private suspend fun handleShippingMethodIdUpdate(paymentData: PaymentData?): Result<Unit> {
        val action = paymentData.mapToShippingOptionIdParams()
        return when {
            action == null -> Result.success(Unit)
            else ->
                shippingMethodUpdateValidator(action)
                    .flatMap { actionInteractor(MultipleActionUpdateParams(params = listOf(action))).map { } }
        }
    }

    private fun closeProxyScreen() =
        composerScope.launch {
            _uiEvent.emit(ComposerUiEvent.Finish)
        }
}
