package io.primer.android.processor3ds.ui

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
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.registerContainer
import io.primer.android.core.di.extensions.unregisterContainer
import io.primer.android.core.di.extensions.viewModel
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.processor3ds.di.Processor3dsContainer
import io.primer.android.processor3ds.presentation.Processor3DSViewModel
import io.primer.android.processor3ds.presentation.Processor3DSViewModelFactory
import io.primer.paymentMethodCoreUi.core.ui.webview.WebViewActivity

class Processor3dsWebViewActivity : WebViewActivity(), DISdkComponent {

    private val viewModel: Processor3DSViewModel
        by viewModel<Processor3DSViewModel, Processor3DSViewModelFactory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerContainer(containerProvider = { Processor3dsContainer(it) })
        logAnalyticsViewed()
        viewModel.getStatus(
            statusUrl = intent?.extras?.getString(STATUS_URL_KEY).orEmpty(),
            paymentMethodType = intent?.extras?.getString(PAYMENT_METHOD_TYPE_KEY).orEmpty()

        )
        setupObservers()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        logBackPressed()
        setResult(
            RESULT_ERROR,
            Intent().apply {
                putExtra(
                    ERROR_KEY,
                    PaymentMethodCancelledException(
                        intent?.extras?.getString(
                            PAYMENT_METHOD_TYPE_KEY
                        ).orEmpty()
                    )
                )
            }
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        logBackPressed()
        setResult(
            RESULT_ERROR,
            Intent().apply {
                putExtra(
                    ERROR_KEY,
                    PaymentMethodCancelledException(
                        intent?.extras?.getString(
                            PAYMENT_METHOD_TYPE_KEY
                        ).orEmpty()
                    )
                )
            }
        )
        return super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterContainer<Processor3dsContainer>()
    }

    override fun setupWebViewClient() {
        val url = intent.extras?.getString(PAYMENT_URL_KEY)
        val captureUrl = intent.extras?.getString(CAPTURE_URL_KEY)
        webView.webViewClient = Processor3dsWebViewClient(
            this,
            url,
            captureUrl
        )
    }

    private fun setupObservers() {
        viewModel.statusUrlLiveData.observe(this) { resumeToken ->
            setResult(
                RESULT_OK,
                Intent().apply {
                    putExtra(RESUME_TOKEN_EXTRA_KEY, resumeToken)
                }
            )
            finish()
        }
        viewModel.statusUrlErrorData.observe(this) { throwable ->
            setResult(
                RESULT_ERROR,
                Intent().apply {
                    putExtra(ERROR_KEY, throwable)
                }
            )
            finish()
        }
    }

    private fun logAnalyticsViewed() = viewModel.addAnalyticsEvent(
        UIAnalyticsParams(
            AnalyticsAction.VIEW,
            ObjectType.WEB_PAGE,
            Place.PAYMENT_METHOD_POPUP,
            context = UrlContextParams(
                url = Uri.parse(
                    intent.extras?.getString(PAYMENT_URL_KEY).orEmpty()
                ).host.orEmpty()
            )
        )
    )

    private fun logBackPressed() = viewModel.addAnalyticsEvent(
        UIAnalyticsParams(
            AnalyticsAction.CLICK,
            ObjectType.BUTTON,
            Place.PAYMENT_METHOD_POPUP,
            ObjectId.BACK
        )
    )

    companion object {

        // url that polls payment status
        const val STATUS_URL_KEY = "STATUS_URL_KEY"
        const val RESUME_TOKEN_EXTRA_KEY = "RESUME_TOKEN"
        const val ERROR_KEY = "ERROR"

        fun getLaunchIntent(
            context: Context,
            paymentUrl: String,
            statusUrl: String,
            title: String,
            paymentMethodType: String
        ): Intent {
            return Intent(context, Processor3dsWebViewActivity::class.java).apply {
                putExtra(PAYMENT_URL_KEY, paymentUrl)
                putExtra(STATUS_URL_KEY, statusUrl)
                putExtra(PAYMENT_METHOD_TYPE_KEY, paymentMethodType)
                putExtra(TOOLBAR_TITLE_KEY, title)
            }
        }
    }
}
