package io.primer.android.webRedirectShared.implementation.composer.presentation

import android.app.Activity
import android.content.Intent
import androidx.annotation.VisibleForTesting
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.paymentmethods.core.composer.composable.UiEventable
import io.primer.android.webRedirectShared.implementation.composer.ui.navigation.launcher.WebRedirectActivityLauncherParams
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityResultIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityStartIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import io.primer.paymentMethodCoreUi.core.ui.webview.WebViewActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

interface BaseWebRedirectComposer :
    PaymentMethodComposer,
    ActivityStartIntentHandler,
    ActivityResultIntentHandler,
    UiEventable {
    val scope: CoroutineScope

    val _uiEvent: MutableSharedFlow<ComposerUiEvent>
    override val uiEvent: SharedFlow<ComposerUiEvent>
        get() = _uiEvent

    fun onResultCancelled(params: WebRedirectLauncherParams)

    fun onResultError(params: WebRedirectLauncherParams, intent: Intent?)

    fun onResultOk(params: WebRedirectLauncherParams)

    override fun handleActivityResultIntent(
        params: PaymentMethodLauncherParams,
        resultCode: Int,
        intent: Intent?,
    ) {
        when (resultCode) {
            Activity.RESULT_CANCELED -> {
                val redirectParams = params.initialLauncherParams as WebRedirectLauncherParams
                onResultCancelled(params = redirectParams)
            }

            Activity.RESULT_OK -> {
                val redirectParams = params.initialLauncherParams as WebRedirectLauncherParams
                onResultOk(params = redirectParams)
            }

            WebViewActivity.RESULT_ERROR -> {
                val redirectParams = params.initialLauncherParams as WebRedirectLauncherParams
                onResultError(params = redirectParams, intent = intent)
            }
        }
        close()
    }

    override fun handleActivityStartEvent(params: PaymentMethodLauncherParams) {
        openRedirectScreen(params.initialLauncherParams as WebRedirectLauncherParams)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun openRedirectScreen(event: WebRedirectLauncherParams) {
        scope.launch {
            _uiEvent.emit(
                ComposerUiEvent.Navigate(
                    WebRedirectActivityLauncherParams(
                        event.statusUrl,
                        event.redirectUrl,
                        event.title,
                        event.paymentMethodType,
                        event.returnUrl,
                    ),
                ),
            )
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun close() =
        scope.launch {
            _uiEvent.emit(ComposerUiEvent.Finish)
        }
}
