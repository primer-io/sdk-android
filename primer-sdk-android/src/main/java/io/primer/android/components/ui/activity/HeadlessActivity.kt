package io.primer.android.components.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import io.primer.android.BaseCheckoutActivity
import io.primer.android.components.domain.BaseEvent
import io.primer.android.components.presentation.HeadlessViewModelFactory
import io.primer.android.components.presentation.NativeUIHeadlessViewModel
import io.primer.android.components.presentation.paymentMethods.nativeUi.googlepay.GooglePayEvent
import io.primer.android.components.ui.extensions.toIPay88LauncherParams
import io.primer.android.di.extension.inject
import io.primer.android.domain.payments.methods.PaymentMethodModulesInteractor
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.ui.base.webview.WebViewClientType
import io.primer.android.ui.mock.PaymentMethodMockActivity
import io.primer.android.ui.payment.async.AsyncPaymentMethodWebViewActivity
import io.primer.ipay88.api.ui.NativeIPay88Activity

internal class HeadlessActivity : BaseCheckoutActivity() {

    private val eventDispatcher: EventDispatcher by inject()
    private val paymentMethodModulesInteractor: PaymentMethodModulesInteractor by inject()
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    private lateinit var viewModel: NativeUIHeadlessViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runIfNotFinishing {
            val params = getLauncherParams() ?: return@runIfNotFinishing
            savedInstanceState?.let {
                intent.putExtra(LAUNCHED_BROWSER_KEY, it.getBoolean(LAUNCHED_BROWSER_KEY))
            }
            val implementationType = paymentMethodModulesInteractor.getPaymentMethodDescriptors()
                .first { it.config.type == params.paymentMethodType }.config.implementationType

            viewModel = HeadlessViewModelFactory().getViewModel(
                this,
                implementationType,
                params.paymentMethodType,
                params.sessionIntent
            ).also { manager ->
                manager.startActivityEvent.observe(this) {
                    eventDispatcher.dispatchEvent(
                        CheckoutEvent.PaymentMethodPresented(params.paymentMethodType)
                    )
                    startRedirect(it)
                }
                manager.finishActivityEvent.observe(this) {
                    finish()
                }
            }
            viewModel.initialize(
                implementationType,
                params.paymentMethodType,
                params.sessionIntent,
                if (savedInstanceState == null) params.initialState else null
            )
            // we don't want to start again in case of config change
            if (savedInstanceState == null && params.initialState == null) {
                viewModel.start(
                    params.paymentMethodType,
                    params.sessionIntent
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(
            LAUNCHED_BROWSER_KEY,
            intent.getBooleanExtra(LAUNCHED_BROWSER_KEY, false)
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onEvent(BaseEvent.OnResult(data, resultCode))
    }

    override fun onNewIntent(newIntent: Intent?) {
        super.onNewIntent(newIntent)
        viewModel.onEvent(BaseEvent.OnBrowserResult(newIntent?.data))
        intent.putExtra(ENTERED_NEW_INTENT_KEY, true)
    }

    override fun onResume() {
        super.onResume()
        if (
            intent.getBooleanExtra(ENTERED_NEW_INTENT_KEY, false).not() &&
            intent.getBooleanExtra(LAUNCHED_BROWSER_KEY, false)
        ) {
            // in case user returns without finalizing flow in browser, we will cancel the flow.
            viewModel.onEvent(BaseEvent.OnBrowserResult(null))
        }
    }

    private fun getLauncherParams() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getSerializableExtra(PARAMS_KEY, PaymentMethodLauncherParams::class.java)
    } else { intent.getSerializableExtra(PARAMS_KEY) as PaymentMethodLauncherParams }

    private fun startRedirect(params: PaymentMethodRedirectLauncherParams) {
        when (params) {
            is IPay88MockActivityLauncherParams -> resultLauncher.launch(
                PaymentMethodMockActivity.getLaunchIntent(this, params.paymentMethodType)
            )
            is WebRedirectActivityLauncherParams -> resultLauncher.launch(
                AsyncPaymentMethodWebViewActivity.getLaunchIntent(
                    this,
                    params.paymentUrl,
                    params.returnUrl,
                    params.title,
                    params.paymentMethodType,
                    WebViewClientType.ASYNC
                )
            )
            is BrowserLauncherParams -> {
                intent.putExtra(LAUNCHED_BROWSER_KEY, true)
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(params.url)))
            }
            is GooglePayActivityLauncherParams -> viewModel.onEvent(
                GooglePayEvent.StartRedirect(this)
            )
            is IPay88ActivityLauncherParams -> resultLauncher.launch(
                NativeIPay88Activity.getLaunchIntent(this, params.toIPay88LauncherParams())
            )
        }
    }

    internal companion object {

        private const val PARAMS_KEY = "LAUNCHER_PARAMS"
        private const val ENTERED_NEW_INTENT_KEY = "ENTERED_NEW_INTENT"
        private const val LAUNCHED_BROWSER_KEY = "LAUNCHED_BROWSER"

        fun getLaunchIntent(
            context: Context,
            params: PaymentMethodLauncherParams
        ): Intent {
            return Intent(context, HeadlessActivity::class.java).putExtra(
                PARAMS_KEY,
                params
            )
        }
    }
}
