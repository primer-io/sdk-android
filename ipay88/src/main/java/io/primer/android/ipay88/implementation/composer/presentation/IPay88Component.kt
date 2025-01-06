package io.primer.android.ipay88.implementation.composer.presentation

import android.app.Activity
import android.content.Intent
import androidx.annotation.VisibleForTesting
import io.primer.android.PrimerSessionIntent
import io.primer.android.configuration.mock.presentation.MockConfigurationDelegate
import io.primer.android.core.extensions.flatMap
import io.primer.android.core.extensions.getSerializableCompat
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.ipay88.implementation.composer.ui.navigation.launcher.IPay88ActivityLauncherParams
import io.primer.android.ipay88.implementation.composer.ui.navigation.launcher.IPay88MockActivityLauncherParams
import io.primer.android.ipay88.implementation.payment.presentation.delegate.presentation.IPay88PaymentDelegate
import io.primer.android.ipay88.implementation.payment.resume.handler.IPay88ResumeHandler
import io.primer.android.ipay88.implementation.tokenization.presentation.IPay88TokenizationDelegate
import io.primer.android.ipay88.implementation.tokenization.presentation.model.IPay88TokenizationInputable
import io.primer.android.paymentmethods.core.composer.InternalNativeUiPaymentMethodComponent
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.payments.core.status.domain.AsyncPaymentMethodPollingInteractor
import io.primer.android.payments.core.status.domain.model.AsyncStatusParams
import io.primer.ipay88.api.ui.NativeIPay88Activity
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityResultIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityStartIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

internal class IPay88Component(
    private val tokenizationDelegate: IPay88TokenizationDelegate,
    private val pollingInteractor: AsyncPaymentMethodPollingInteractor,
    private val paymentDelegate: IPay88PaymentDelegate,
    private val mockConfigurationDelegate: MockConfigurationDelegate,
) : InternalNativeUiPaymentMethodComponent(),
    ActivityStartIntentHandler,
    ActivityResultIntentHandler {
    override fun start(
        paymentMethodType: String,
        primerSessionIntent: PrimerSessionIntent,
    ) {
        this.paymentMethodType = paymentMethodType
        this.primerSessionIntent = primerSessionIntent
        composerScope.launch {
            paymentDelegate.uiEvent.collectLatest { uiEvent ->
                _uiEvent.emit(uiEvent)
            }
        }
        tokenize(paymentMethodType)
    }

    override fun handleActivityResultIntent(
        params: PaymentMethodLauncherParams,
        resultCode: Int,
        intent: Intent?,
    ) {
        when (resultCode) {
            Activity.RESULT_CANCELED -> {
                composerScope.launch {
                    paymentDelegate.handleError(
                        PaymentMethodCancelledException(
                            params.paymentMethodType,
                        ),
                    )
                }
            }

            Activity.RESULT_OK -> {
                val redirectParams = params.initialLauncherParams as RedirectLauncherParams
                startPolling(redirectParams.statusUrl, redirectParams.paymentMethodType)
            }

            IPay88ResumeHandler.RESULT_ERROR_CODE -> {
                composerScope.launch {
                    paymentDelegate.handleError(
                        requireNotNull(intent?.getSerializableCompat<Exception>(NativeIPay88Activity.ERROR_KEY)),
                    )
                }
            }
        }
        close()
    }

    override fun handleActivityStartEvent(params: PaymentMethodLauncherParams) {
        openRedirectScreen(params.initialLauncherParams as RedirectLauncherParams)
    }

    private fun tokenize(paymentMethodType: String) =
        composerScope.launch {
            tokenizationDelegate.tokenize(
                IPay88TokenizationInputable(
                    paymentMethodType = paymentMethodType,
                    primerSessionIntent = primerSessionIntent,
                ),
            )
                .flatMap { paymentMethodTokenData ->
                    paymentDelegate.handlePaymentMethodToken(
                        paymentMethodTokenData = paymentMethodTokenData,
                        primerSessionIntent = primerSessionIntent,
                    )
                }.onFailure { throwable ->
                    paymentDelegate.handleError(throwable)
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun startPolling(
        statusUrl: String,
        paymentMethodType: String,
    ) = composerScope.launch {
        pollingInteractor.execute(
            AsyncStatusParams(statusUrl, paymentMethodType),
        ).mapLatest { status ->
            paymentDelegate.resumePayment(status.resumeToken)
        }.catch {
            paymentDelegate.handleError(it)
        }.collect()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun openRedirectScreen(event: RedirectLauncherParams) {
        composerScope.launch {
            _uiEvent.emit(
                ComposerUiEvent.Navigate(
                    if (mockConfigurationDelegate.isMockedFlow()) {
                        IPay88MockActivityLauncherParams(
                            errorCode = event.errorCode,
                            sessionIntent = event.sessionIntent,
                        )
                    } else {
                        IPay88ActivityLauncherParams(
                            statusUrl = event.statusUrl,
                            iPayPaymentId = event.iPayPaymentId,
                            iPayMethod = event.iPayMethod,
                            merchantCode = event.merchantCode,
                            actionType = event.actionType,
                            amount = event.amount,
                            referenceNumber = event.referenceNumber,
                            prodDesc = event.prodDesc,
                            currencyCode = event.currencyCode,
                            countryCode = event.countryCode,
                            customerName = event.customerName,
                            customerEmail = event.customerEmail,
                            remark = event.remark,
                            backendCallbackUrl = event.backendCallbackUrl,
                            deeplinkUrl = event.deeplinkUrl,
                            errorCode = event.errorCode,
                            paymentMethodType = event.paymentMethodType,
                            sessionIntent = event.sessionIntent,
                        )
                    },
                ),
            )
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun close() =
        composerScope.launch {
            _uiEvent.emit(ComposerUiEvent.Finish)
        }
}
