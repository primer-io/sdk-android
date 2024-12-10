package io.primer.android.card.implementation.composer

import android.app.Activity
import android.content.Intent
import io.primer.android.PrimerSessionIntent
import io.primer.android.card.implementation.composer.ui.navigation.CardNative3DSActivityLauncherParams
import io.primer.android.card.implementation.composer.ui.navigation.CardProcessor3DSActivityLauncherParams
import io.primer.android.card.implementation.payment.delegate.CardPaymentDelegate
import io.primer.android.card.implementation.payment.delegate.ProcessorThreeDsInitialLauncherParams
import io.primer.android.card.implementation.payment.delegate.ThreeDsInitialLauncherParams
import io.primer.android.core.extensions.getSerializableCompat
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.paymentmethods.core.composer.VaultedPaymentMethodComponent
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.paymentmethods.core.composer.composable.UiEventable
import io.primer.android.processor3ds.ui.Processor3dsWebViewActivity
import io.primer.android.threeds.ui.ThreeDsActivity
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityResultIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityStartIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import io.primer.paymentMethodCoreUi.core.ui.webview.WebViewActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

internal class VaultedCardComponent(
    override val paymentDelegate: CardPaymentDelegate
) : VaultedPaymentMethodComponent,
    ActivityResultIntentHandler,
    ActivityStartIntentHandler,
    UiEventable {

    private val composerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var primerSessionIntent by Delegates.notNull<PrimerSessionIntent>()
    private var paymentMethodType by Delegates.notNull<String>()

    private val _uiEvent = MutableSharedFlow<ComposerUiEvent>()
    override val uiEvent: SharedFlow<ComposerUiEvent> = _uiEvent

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

    override fun cancel() {
        composerScope.cancel()
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
                is ThreeDsInitialLauncherParams ->
                    _uiEvent.emit(
                        ComposerUiEvent.Navigate(
                            CardNative3DSActivityLauncherParams(
                                paymentMethodType = paymentMethodType,
                                supportedThreeDsVersions = initialLaunchEvents.supportedThreeDsProtocolVersions
                            )
                        )
                    )

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

    private fun closeProxyScreen() = composerScope.launch {
        _uiEvent.emit(ComposerUiEvent.Finish)
    }
}
