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
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.di.DIAppComponent
import io.primer.android.presentation.payment.async.AsyncPaymentMethodViewModel
import io.primer.android.ui.base.webview.WebViewActivity
import io.primer.android.ui.base.webview.WebViewClientType
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class AsyncPaymentMethodWebViewActivity : WebViewActivity(), DIAppComponent {

    private val asyncPaymentMethodViewModel: AsyncPaymentMethodViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logAnalyticsViewed()
        asyncPaymentMethodViewModel.getStatus(
            intent?.extras?.getString(STATUS_URL_KEY).orEmpty(),
            intent?.extras?.getSerializable(PAYMENT_METHOD_TYPE_KEY) as PaymentMethodType

        )
        setupObservers()
    }

    override fun onSupportNavigateUp(): Boolean {
        logBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun setupObservers() {
        asyncPaymentMethodViewModel.statusUrlLiveData.observe(this) {
            setResult(RESULT_OK)
            finish()
        }
        asyncPaymentMethodViewModel.statusUrlErrorData.observe(this) {
            setResult(RESULT_ERROR)
            finish()
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

        // url that polls payment status
        const val STATUS_URL_KEY = "STATUS_URL_KEY"

        fun getLaunchIntent(
            context: Context,
            paymentUrl: String,
            captureUrl: String,
            statusUrl: String,
            title: String,
            paymentMethodType: PaymentMethodType,
            webViewClientType: WebViewClientType,
        ): Intent {
            return Intent(context, AsyncPaymentMethodWebViewActivity::class.java).apply {
                putExtra(PAYMENT_URL_KEY, paymentUrl)
                putExtra(CAPTURE_URL_KEY, captureUrl)
                putExtra(STATUS_URL_KEY, statusUrl)
                putExtra(PAYMENT_METHOD_TYPE_KEY, paymentMethodType)
                putExtra(TOOLBAR_TITLE_KEY, title)
                putExtra(WEB_VIEW_CLIENT_TYPE, webViewClientType)
            }
        }
    }
}
