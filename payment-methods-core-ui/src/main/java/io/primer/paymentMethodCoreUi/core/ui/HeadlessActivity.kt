package io.primer.paymentMethodCoreUi.core.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.inject
import io.primer.android.core.extensions.getSerializableCompat
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.paymentmethods.core.composer.composable.UiEventable
import io.primer.android.paymentmethods.core.composer.registry.PaymentMethodComposerRegistry
import io.primer.android.paymentmethods.core.composer.registry.VaultedPaymentMethodComposerRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityResultIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityStartIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.PaymentMethodContextNavigationHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import kotlinx.coroutines.launch

class HeadlessActivity : BaseCheckoutActivity(), DISdkComponent {
    private val paymentMethodComposerRegistry: PaymentMethodComposerRegistry by inject()
    private val vaultedPaymentMethodComposerRegistry: VaultedPaymentMethodComposerRegistry by inject()

    private val paymentMethodNavigationFactoryRegistry: PaymentMethodNavigationFactoryRegistry by inject()
    private val tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository by inject()

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        }

    private lateinit var composer: PaymentMethodComposer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val params =
            getLauncherParams() ?: run {
                finish()
                return
            }
        runIfNotFinishing {
            val isVaultedPaymentMethod =
                runCatching {
                    tokenizedPaymentMethodRepository.getPaymentMethod().isVaulted
                }.getOrNull() ?: false
            composer = when (isVaultedPaymentMethod) {
                false -> paymentMethodComposerRegistry[params.paymentMethodType]
                true -> vaultedPaymentMethodComposerRegistry[params.paymentMethodType]
            } ?: error("Cannot resolve composer for ${params.paymentMethodType}")

            savedInstanceState?.let {
                intent.putExtra(LAUNCHED_BROWSER_KEY, it.getBoolean(LAUNCHED_BROWSER_KEY))
            }

//        // TODO check configuration changes
//         we don't want to start again in case of config change
//        if (savedInstanceState == null && params.initialState == null) {
//            viewModel.start(
//                params.paymentMethodType,
//                params.sessionIntent
//            )
//        }

            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                    val uiEventable: UiEventable = composer as UiEventable
                    uiEventable.uiEvent.collect { event ->
                        when (event) {
                            is ComposerUiEvent.Finish -> finish()
                            is ComposerUiEvent.Navigate ->
                                (
                                    paymentMethodNavigationFactoryRegistry.create(
                                        params.paymentMethodType,
                                    ) as? PaymentMethodContextNavigationHandler
                                )?.getSupportedNavigators(this@HeadlessActivity, resultLauncher)
                                    ?.firstOrNull { it.canHandle(event.params) }?.navigate(event.params)
                        }
                    }
                }
            }

            val activityResultIntentHandler = composer as ActivityStartIntentHandler
            activityResultIntentHandler.handleActivityStartEvent(params)
        }
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
