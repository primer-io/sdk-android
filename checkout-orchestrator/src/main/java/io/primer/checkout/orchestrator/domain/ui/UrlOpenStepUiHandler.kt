package io.primer.checkout.orchestrator.domain.ui

import android.app.Activity
import android.content.Intent
import io.primer.android.PrimerSessionIntent
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.webRedirectShared.implementation.composer.presentation.WebRedirectLauncherParams
import io.primer.android.webRedirectShared.implementation.composer.ui.activity.WebRedirectActivity
import io.primer.android.webRedirectShared.implementation.composer.ui.navigation.launcher.WebRedirectActivityLauncherParams
import io.primer.checkout.orchestrator.domain.ReturnUriProvider
import io.primer.executionengine.domain.handler.UrlCloseReason
import io.primer.executionengine.domain.handler.UrlOpenHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import io.primer.paymentMethodCoreUi.core.ui.webview.WebViewActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class UrlOpenStepUiHandler(
    private val urlOpenHandler: UrlOpenHandler,
    private val returnUriProvider: ReturnUriProvider,
) : StepUiHandler {

    override fun observeLaunchRequests(
        scope: CoroutineScope,
        paymentMethodType: String,
        emitter: suspend (ComposerUiEvent) -> Unit,
    ) {
        scope.launch {
            urlOpenHandler.launchRequest.collectLatest { request ->
                emitter(
                    ComposerUiEvent.Navigate(
                        PaymentMethodLauncherParams(
                            paymentMethodType = paymentMethodType,
                            sessionIntent = PrimerSessionIntent.CHECKOUT,
                            initialLauncherParams = WebRedirectLauncherParams(
                                title = "",
                                paymentMethodType = paymentMethodType,
                                redirectUrl = request.url,
                                statusUrl = "",
                                returnUrl = returnUriProvider.provide(),
                            ),
                        ),
                    ),
                )
            }
        }
    }

    override fun handleActivityResult(
        params: PaymentMethodLauncherParams,
        resultCode: Int,
        intent: Intent?,
    ): Boolean {
        when (resultCode) {
            Activity.RESULT_OK -> urlOpenHandler.onClosed(UrlCloseReason.AUTO)
            Activity.RESULT_CANCELED -> urlOpenHandler.onClosed(intent.toCloseReason())
            WebViewActivity.RESULT_ERROR -> urlOpenHandler.onResultError(intent?.dataString.orEmpty())
        }
        return true
    }

    /**
     * RESULT_CANCELED carrying the redirect-return extra comes from a deep-link return, so the
     * browser closed automatically; otherwise USER is the spec-sanctioned safe default.
     */
    private fun Intent?.toCloseReason(): UrlCloseReason =
        if (this?.getBooleanExtra(WebRedirectActivity.REDIRECT_RETURN_EXTRA_KEY, false) == true) {
            UrlCloseReason.AUTO
        } else {
            UrlCloseReason.USER
        }

    override fun handleActivityStartEvent(
        params: PaymentMethodLauncherParams,
    ): ComposerUiEvent? {
        val launcherParams = params.initialLauncherParams as? WebRedirectLauncherParams ?: return null
        return ComposerUiEvent.Navigate(
            WebRedirectActivityLauncherParams(
                statusUrl = launcherParams.statusUrl,
                paymentUrl = launcherParams.redirectUrl,
                title = launcherParams.title,
                paymentMethodType = launcherParams.paymentMethodType,
                returnUrl = launcherParams.returnUrl,
                redirectUrls = null,
            ),
        )
    }
}
