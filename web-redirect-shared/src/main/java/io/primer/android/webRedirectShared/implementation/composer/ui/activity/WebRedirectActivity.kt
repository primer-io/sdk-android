package io.primer.android.webRedirectShared.implementation.composer.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.analytics.domain.models.UrlContextParams
import io.primer.android.core.di.extensions.viewModel
import io.primer.android.webRedirectShared.implementation.composer.presentation.viewmodel.WebRedirectViewModel
import io.primer.android.webRedirectShared.implementation.composer.presentation.viewmodel.WebRedirectViewModelFactory
import io.primer.paymentMethodCoreUi.core.ui.webview.WebViewActivity

class WebRedirectActivity : WebViewActivity() {
    private val viewModel: WebRedirectViewModel
        by viewModel<WebRedirectViewModel, WebRedirectViewModelFactory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runIfNotFinishing {
            logAnalyticsViewed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        logBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        logNewIntentCalled(intent = intent)
        when (
            intent?.data?.pathSegments?.contains(
                WebRedirectPaymentMethodWebViewClient.CANCEL_STATE_QUERY_PARAM,
            )
        ) {
            true -> setResult(RESULT_CANCELED)
            else -> setResult(RESULT_OK)
        }
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        logBackPressed()
        super.onBackPressed()
    }

    override fun setupWebViewClient() {
        val url = intent.extras?.getString(PAYMENT_URL_KEY)
        val captureUrl = intent.extras?.getString(CAPTURE_URL_KEY)
        webView.webViewClient =
            WebRedirectPaymentMethodWebViewClient(
                activity = this,
                url = url,
                returnUrl = captureUrl,
            )
    }

    private fun logAnalyticsViewed() =
        viewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.VIEW,
                ObjectType.WEB_PAGE,
                Place.PAYMENT_METHOD_POPUP,
                context =
                UrlContextParams(
                    intent.extras?.getString(PAYMENT_URL_KEY).orEmpty().toUri().host.orEmpty(),
                ),
            ),
        )

    private fun logNewIntentCalled(intent: Intent?) =
        viewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.NEW_INTENT,
                ObjectType.WEB_PAGE,
                Place.PAYMENT_METHOD_POPUP,
                context = UrlContextParams(intent?.data.toString()),
            ),
        )

    private fun logBackPressed() =
        viewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.CLICK,
                ObjectType.BUTTON,
                Place.PAYMENT_METHOD_POPUP,
                ObjectId.BACK,
            ),
        )

    companion object {
        fun getLaunchIntent(
            context: Context,
            paymentUrl: String,
            deeplinkUrl: String,
            title: String,
            paymentMethodType: String,
        ): Intent {
            return Intent(context, WebRedirectActivity::class.java).apply {
                putExtra(PAYMENT_URL_KEY, paymentUrl)
                putExtra(CAPTURE_URL_KEY, deeplinkUrl)
                putExtra(PAYMENT_METHOD_TYPE_KEY, paymentMethodType)
                putExtra(TOOLBAR_TITLE_KEY, title)
            }
        }
    }
}
