package io.primer.android.ui.payment.async

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.analytics.domain.models.UrlContextParams
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.presentation.payment.async.AsyncPaymentMethodViewModel
import io.primer.android.ui.base.webview.WebViewActivity
import io.primer.android.ui.base.webview.WebViewClientType
import org.koin.androidx.viewmodel.ext.android.viewModel

internal class AsyncPaymentMethodWebViewActivity : WebViewActivity() {

    private var subscription: EventBus.SubscriptionHandle? = null

    private val asyncPaymentMethodViewModel: AsyncPaymentMethodViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logAnalyticsViewed()
        setupObservers()
    }

    override fun onSupportNavigateUp(): Boolean {
        logBackPressed()
        return super.onSupportNavigateUp()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        logBackPressed()
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        subscription?.unregister()
        subscription = null
    }

    private fun setupObservers() {
        subscription = EventBus.subscribe {
            when (it) {
                is CheckoutEvent.AsyncFlowRedirect -> {
                    when (
                        it.uri?.pathSegments?.contains(
                            AsyncPaymentMethodWebViewClient.CANCEL_STATE_QUERY_PARAM
                        )
                    ) {
                        true -> setResult(RESULT_CANCELED)
                        else -> setResult(RESULT_OK)
                    }
                    finish()
                }
                is CheckoutEvent.AsyncFlowPollingError -> {
                    setResult(RESULT_ERROR)
                    finish()
                }
                else -> Unit
            }
        }
    }

    private fun logAnalyticsViewed() = asyncPaymentMethodViewModel.addAnalyticsEvent(
        UIAnalyticsParams(
            AnalyticsAction.VIEW,
            ObjectType.WEB_PAGE,
            Place.PAYMENT_METHOD_POPUP,
            context = UrlContextParams(
                Uri.parse(
                    intent.extras?.getString(PAYMENT_URL_KEY).orEmpty()
                ).host.orEmpty()
            )
        )
    )

    private fun logBackPressed() = asyncPaymentMethodViewModel.addAnalyticsEvent(
        UIAnalyticsParams(
            AnalyticsAction.CLICK,
            ObjectType.BUTTON,
            Place.PAYMENT_METHOD_POPUP,
            ObjectId.BACK
        )
    )

    internal companion object {

        fun getLaunchIntent(
            context: Context,
            paymentUrl: String,
            deeplinkUrl: String,
            title: String,
            paymentMethodType: String,
            webViewClientType: WebViewClientType,
        ): Intent {
            return Intent(context, AsyncPaymentMethodWebViewActivity::class.java).apply {
                putExtra(PAYMENT_URL_KEY, paymentUrl)
                putExtra(CAPTURE_URL_KEY, deeplinkUrl)
                putExtra(PAYMENT_METHOD_TYPE_KEY, paymentMethodType)
                putExtra(TOOLBAR_TITLE_KEY, title)
                putExtra(WEB_VIEW_CLIENT_TYPE, webViewClientType)
            }
        }
    }
}
