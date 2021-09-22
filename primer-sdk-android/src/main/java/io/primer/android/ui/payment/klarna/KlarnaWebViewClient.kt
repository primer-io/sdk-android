package io.primer.android.ui.payment.klarna

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.WebResourceRequest
import androidx.core.net.toUri
import io.primer.android.ui.base.webview.WebViewActivity
import io.primer.android.ui.base.webview.BaseWebViewClient

internal class KlarnaWebViewClient(
    activity: WebViewActivity,
    val url: String?,
    val returnUrl: String?,
) : BaseWebViewClient(activity, url, returnUrl) {

    override fun getUrlState(url: String): UrlState {
        return when (url.toUri().getQueryParameter(STATE_QUERY_PARAM_KEY)) {
            CANCEL_STATE_QUERY_PARAM -> UrlState.CANCELLED
            SUCCESS_STATE_QUERY_PARAM -> UrlState.SUCCESS
            else -> UrlState.ERROR
        }
    }

    override fun getCaptureUrl(url: String?): String? {
        return url?.let { Uri.parse(url).scheme.toString() }
    }

    override fun handleDeepLink(request: WebResourceRequest?): Boolean {
        request?.url?.let { uri ->
            if (uri.scheme.orEmpty().contains(BANKID_SCHEME)) {
                handleIntent(
                    Intent(Intent.ACTION_VIEW).apply {
                        data = uri
                    }
                )
                return true
            } else super.handleDeepLink(request)
        }

        return true
    }

    override fun cannotHandleIntent(intent: Intent) {
        Log.e(TAG, "Cannot handle intent: ${intent.data}")
    }

    private companion object {

        // https://www.bankid.com/assets/bankid/rp/bankid-relying-party-guidelines-v3.5.pdf
        const val BANKID_SCHEME = "bankid"

        const val STATE_QUERY_PARAM_KEY = "state"

        const val CANCEL_STATE_QUERY_PARAM = "cancel"

        const val SUCCESS_STATE_QUERY_PARAM = "success"
    }
}
