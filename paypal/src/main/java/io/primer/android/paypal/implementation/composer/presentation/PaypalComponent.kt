package io.primer.android.paypal.implementation.composer.presentation

import android.content.Intent
import android.net.Uri
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.extensions.flatMap
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.paymentmethods.core.composer.InternalNativeUiPaymentMethodComponent
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.paypal.implementation.composer.ui.navigation.launcher.BrowserLauncherParams
import io.primer.android.paypal.implementation.payment.presentation.delegate.presentation.PaypalPaymentDelegate
import io.primer.android.paypal.implementation.tokenization.presentation.PaypalTokenizationCollectorDelegate
import io.primer.android.paypal.implementation.tokenization.presentation.PaypalTokenizationCollectorParams
import io.primer.android.paypal.implementation.tokenization.presentation.PaypalTokenizationDelegate
import io.primer.android.paypal.implementation.tokenization.presentation.model.PaypalTokenizationInputable
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityResultIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityStartIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

internal class PaypalComponent(
    private val tokenizationCollectorDelegate: PaypalTokenizationCollectorDelegate,
    private val tokenizationDelegate: PaypalTokenizationDelegate,
    private val paymentDelegate: PaypalPaymentDelegate,
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
            tokenizationCollectorDelegate.uiEvent.collect {
                _uiEvent.emit(it)
            }
        }

        composerScope.launch {
            try {
                tokenizationCollectorDelegate.startDataCollection(
                    params =
                    PaypalTokenizationCollectorParams(
                        primerSessionIntent = primerSessionIntent,
                    ),
                ).onFailure { throwable ->
                    paymentDelegate.handleError(throwable)
                }
            } catch (ignored: CancellationException) {
                paymentDelegate.handleError(PaymentMethodCancelledException(paymentMethodType = paymentMethodType))
            }
        }
    }

    override fun handleActivityResultIntent(
        params: PaymentMethodLauncherParams,
        resultCode: Int,
        intent: Intent?,
    ) {
        val redirectParams = params.initialLauncherParams as RedirectLauncherParams
        when (intent?.data?.buildUpon()?.clearQuery()?.build()) {
            Uri.parse(redirectParams.successUrl) -> {
                tokenize(
                    when (params.sessionIntent) {
                        PrimerSessionIntent.CHECKOUT ->
                            PaypalTokenizationInputable.PaypalCheckoutTokenizationInputable(
                                orderId = intent?.data?.getQueryParameter(TOKEN_QUERY_PARAM),
                                paymentMethodType = paymentMethodType,
                                paymentMethodConfigId = redirectParams.paymentMethodConfigId,
                                primerSessionIntent = primerSessionIntent,
                            )

                        PrimerSessionIntent.VAULT ->
                            PaypalTokenizationInputable.PaypalVaultTokenizationInputable(
                                tokenId = intent?.data?.getQueryParameter(BA_TOKEN_QUERY_PARAM),
                                paymentMethodConfigId = redirectParams.paymentMethodConfigId,
                                paymentMethodType = paymentMethodType,
                                primerSessionIntent = primerSessionIntent,
                            )
                    },
                )
            }

            else -> {
                composerScope.launch {
                    paymentDelegate.handleError(
                        PaymentMethodCancelledException(
                            params.paymentMethodType,
                        ),
                    )
                }
            }
        }
        close()
    }

    override fun handleActivityStartEvent(params: PaymentMethodLauncherParams) {
        openRedirectScreen(params.initialLauncherParams as RedirectLauncherParams)
    }

    private fun tokenize(tokenizationInputable: PaypalTokenizationInputable) =
        composerScope.launch {
            try {
                tokenizationDelegate.tokenize(tokenizationInputable)
                    .flatMap { paymentMethodTokenData ->
                        paymentDelegate.handlePaymentMethodToken(
                            paymentMethodTokenData = paymentMethodTokenData,
                            primerSessionIntent = primerSessionIntent,
                        )
                    }.onFailure { throwable ->
                        paymentDelegate.handleError(throwable)
                    }
            } catch (ignored: CancellationException) {
                paymentDelegate.handleError(PaymentMethodCancelledException(paymentMethodType = paymentMethodType))
            }
        }

    private fun openRedirectScreen(event: RedirectLauncherParams) {
        composerScope.launch {
            _uiEvent.emit(
                ComposerUiEvent.Navigate(
                    BrowserLauncherParams(
                        url = event.url,
                        host = event.successUrl,
                        paymentMethodType = event.paymentMethodType,
                        sessionIntent = event.sessionIntent,
                    ),
                ),
            )
        }
    }

    private fun close() =
        composerScope.launch {
            _uiEvent.emit(ComposerUiEvent.Finish)
        }

    private companion object {
        private const val TOKEN_QUERY_PARAM = "token"
        private const val BA_TOKEN_QUERY_PARAM = "ba_token"
    }
}
