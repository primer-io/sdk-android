package io.primer.paymentMethodCoreUi.core.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.extensions.getSerializableCompat
import io.primer.android.core.utils.CoroutineScopeProvider
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.paymentmethods.core.composer.composable.UiEventable
import io.primer.android.paymentmethods.core.composer.registry.PaymentMethodComposerRegistry
import io.primer.android.paymentmethods.core.composer.registry.VaultedPaymentMethodComposerRegistry
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityResultIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityStartIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.PaymentMethodContextNavigationHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

/**
 * Invisible proxy activity hosting the payment method flows that need an activity (3DS, web views, native SDK
 * sheets). It is started by the SDK and must not outlive the SDK instance that started it.
 */
class HeadlessActivity : BaseCheckoutActivity(), DISdkComponent {
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        }

    private lateinit var composer: PaymentMethodComposer
    private lateinit var paymentMethodNavigationFactoryRegistry: PaymentMethodNavigationFactoryRegistry

    /** Child of the SDK session scope; lives exactly as long as the SDK instance that started this activity. */
    private lateinit var sdkSessionJob: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val params =
            getLauncherParams() ?: run {
                finish()
                return
            }
        runIfNotFinishing {
            if (finishIfSdkIsNotInitialized() || resolveDependencies(params.paymentMethodType).not()) {
                return@runIfNotFinishing
            }

            savedInstanceState?.let {
                intent.putExtra(LAUNCHED_BROWSER_KEY, it.getBoolean(LAUNCHED_BROWSER_KEY))
            }

            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                    launch {
                        try {
                            // cleanup() cancels the children of the SDK session scope; that is our signal to close.
                            sdkSessionJob.join()
                            logReporter.warn(
                                "Finishing activity (hashcode ${hashCode()}) because the SDK that started it " +
                                    "was cleaned up",
                            )
                            finish()
                        } finally {
                            // Destroyed before the SDK was cleaned up: leave no orphan behind in the SDK scope.
                            sdkSessionJob.cancel()
                        }
                    }
                    collectUiEvents(params)
                }
            }

            // Only the instance that starts the flow kicks it off. An instance recreated by the system
            // (configuration change, activity destroyed in the background) receives the result of the flow that
            // is already running; re-emitting the start event would launch that flow a second time.
            if (savedInstanceState == null) {
                (composer as ActivityStartIntentHandler).handleActivityStartEvent(params)
            }
        }
    }

    /**
     * Resolves everything this activity needs while the SDK instance is known to be alive, so nothing is resolved
     * later from a container that `cleanup()` may have cleared in the meantime. Finishes the activity and returns
     * false when the dependencies cannot be resolved.
     */
    private fun resolveDependencies(paymentMethodType: String): Boolean =
        runCatching {
            val isVaultedPaymentMethod =
                runCatching { resolve<TokenizedPaymentMethodRepository>().getPaymentMethod().isVaulted }
                    .getOrNull() ?: false
            composer = when (isVaultedPaymentMethod) {
                false -> resolve<PaymentMethodComposerRegistry>()[paymentMethodType]
                true -> resolve<VaultedPaymentMethodComposerRegistry>()[paymentMethodType]
            } ?: error("Cannot resolve composer for $paymentMethodType")
            paymentMethodNavigationFactoryRegistry = resolve()
            sdkSessionJob = resolve<CoroutineScopeProvider>().scope.launch { awaitCancellation() }
        }.onFailure { throwable ->
            logReporter.error(
                message = "Finishing activity (hashcode ${hashCode()}) because its dependencies cannot be resolved",
                throwable = throwable,
            )
            finish()
        }.isSuccess

    private suspend fun collectUiEvents(params: PaymentMethodLauncherParams) {
        (composer as UiEventable).uiEvent.collect { event ->
            when (event) {
                is ComposerUiEvent.Finish -> finish()
                is ComposerUiEvent.Navigate -> navigate(params.paymentMethodType, event.params)
            }
        }
    }

    private fun navigate(
        paymentMethodType: String,
        navigationParams: NavigationParams,
    ) {
        // Never start dependent screens (3DS, web views) from an activity that is on its way out or whose SDK
        // instance is already gone.
        if (isFinishing || DISdkContext.isHeadlessInitialized.not()) return
        (paymentMethodNavigationFactoryRegistry.create(paymentMethodType) as? PaymentMethodContextNavigationHandler)
            ?.getSupportedNavigators(this, resultLauncher)
            ?.firstOrNull { it.canHandle(navigationParams) }
            ?.navigate(navigationParams)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(
            LAUNCHED_BROWSER_KEY,
            intent.getBooleanExtra(LAUNCHED_BROWSER_KEY, false),
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        handleActivityResult(resultCode = resultCode, data = data)
    }

    override fun onNewIntent(newIntent: Intent?) {
        super.onNewIntent(newIntent)
        intent.putExtra(ENTERED_NEW_INTENT_KEY, true)
        handleActivityResult(resultCode = RESULT_OK, data = newIntent)
    }

    override fun onResume() {
        super.onResume()
        if (
            intent.getBooleanExtra(ENTERED_NEW_INTENT_KEY, false).not() &&
            intent.getBooleanExtra(LAUNCHED_BROWSER_KEY, false)
        ) {
            // in case user returns without finalizing flow in browser, we will cancel the flow.
            handleActivityResult(resultCode = RESULT_CANCELED, data = null)
        }
    }

    private fun handleActivityResult(
        resultCode: Int,
        data: Intent?,
    ) {
        if (::composer.isInitialized.not()) {
            finish()
            return
        }
        val activityResultIntentHandler = composer as ActivityResultIntentHandler
        getLauncherParams()?.let { params ->
            activityResultIntentHandler.handleActivityResultIntent(
                params = params,
                resultCode = resultCode,
                intent = data,
            )
        } ?: run { finish() }
    }

    private fun getLauncherParams() = intent.getSerializableCompat<PaymentMethodLauncherParams>(name = PARAMS_KEY)

    companion object {
        private const val PARAMS_KEY = "LAUNCHER_PARAMS"
        private const val ENTERED_NEW_INTENT_KEY = "ENTERED_NEW_INTENT"
        const val LAUNCHED_BROWSER_KEY = "LAUNCHED_BROWSER"

        fun getLaunchIntent(
            context: Context,
            params: PaymentMethodLauncherParams,
        ): Intent {
            return Intent(context, HeadlessActivity::class.java).putExtra(
                PARAMS_KEY,
                params,
            )
        }
    }
}
