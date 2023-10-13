package io.primer.android.ui.payment.processor3ds

import android.content.Intent
import android.net.Uri
import android.util.Log
import io.primer.android.ui.base.webview.BaseWebViewClient
import io.primer.android.ui.base.webview.WebViewActivity

internal class Processor3dsWebViewClient(
    activity: WebViewActivity,
    url: String?,
    returnUrl: String?
) : BaseWebViewClient(activity, url, returnUrl) {

    override fun getUrlState(url: String) = UrlState.PROCESSING

    override fun getCaptureUrl(url: String?) = url

    override fun canCaptureUrl(url: String?) = false

    override fun handleDeepLink(uri: Uri?): Boolean {
        getIntentFromUri(uri)?.let {
            handleIntent(it)
        }

        return true
    }

    override fun cannotHandleIntent(intent: Intent) {
        Log.e(TAG, "Cannot handle intent: ${intent.data}")
    }
}
