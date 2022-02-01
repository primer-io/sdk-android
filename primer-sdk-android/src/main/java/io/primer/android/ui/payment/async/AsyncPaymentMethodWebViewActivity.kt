package io.primer.android.ui.payment.async

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
        asyncPaymentMethodViewModel.getStatus(
            intent?.extras?.getString(STATUS_URL_KEY).orEmpty()
        )
        setupObservers()
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

    internal companion object {

        // url that polls payment status
        const val STATUS_URL_KEY = "STATUS_URL_KEY"

        fun getLaunchIntent(
            context: Context,
            paymentUrl: String,
            captureUrl: String,
            statusUrl: String,
            title: String,
            webViewClientType: WebViewClientType,
        ): Intent {
            return Intent(context, AsyncPaymentMethodWebViewActivity::class.java).apply {
                putExtra(PAYMENT_URL_KEY, paymentUrl)
                putExtra(CAPTURE_URL_KEY, captureUrl)
                putExtra(STATUS_URL_KEY, statusUrl)
                putExtra(TOOLBAR_TITLE_KEY, title)
                putExtra(WEB_VIEW_CLIENT_TYPE, webViewClientType)
            }
        }
    }
}
