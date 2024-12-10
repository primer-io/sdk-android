package io.primer.paymentMethodCoreUi.core.ui.webview

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.URLUtil
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri

abstract class BaseWebViewClient(
    private val activity: WebViewActivity,
    private val url: String?,
    private val returnUrl: String?
) : WebViewClient() {

    private val browserApps by lazy {
        activity.packageManager.queryIntentActivities(
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(SAMPLE_URL)
            },
            0
        ).map { it.activityInfo.packageName }.toSet()
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val isDeeplink = URLUtil.isNetworkUrl(request?.url?.toString().orEmpty()).not() ||
            canAnyAppHandleUrl(request?.url)
        return if (isDeeplink) {
            handleDeepLink(request?.url)
        } else {
            handleNetworkUrl(request)
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressWarnings("deprecation")
    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        handleError(failingUrl, errorCode)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        handleError(request?.url?.toString(), error?.errorCode)
    }

    abstract fun getUrlState(url: String): UrlState

    abstract fun getCaptureUrl(url: String?): String?

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    open fun handleDeepLink(uri: Uri?): Boolean {
        val intent = Intent(Intent.ACTION_VIEW)
        uri.let { uri ->
            intent.apply { data = uri }
        }
        intent.data?.let { data ->
            if (canCaptureUrl(data.scheme)) {
                onUrlCaptured(intent)
            }
        }
        return true
    }

    protected open fun handleNetworkUrl(request: WebResourceRequest?): Boolean {
        val requestUrl = request?.url?.toString()
        val shouldOverride = canCaptureUrl(requestUrl)
        if (shouldOverride) {
            requestUrl?.let {
                onUrlCaptured(Intent().apply { data = it.toUri() })
            }
        }
        return shouldOverride
    }

    protected open fun handleResult(resultCode: Int, intent: Intent) {
        activity.apply {
            setResult(resultCode, intent)
            finish()
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    open fun handleIntent(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Log.d(TAG, "Android 11+, data: ${intent.data}")
            handleIntentOnAndroid11OrAbove(intent)
        } else {
            Log.d(TAG, "Android 10-, data: ${intent.data}")
            handleIntentOnAndroid10OrBelow(intent)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    open fun cannotHandleIntent(intent: Intent) {
        Log.e(TAG, "Cannot handle intent: ${intent.data}")
        activity.apply {
            setResult(WebViewActivity.RESULT_ERROR, intent)
            finish()
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    open fun onUrlCaptured(intent: Intent) {
        try {
            val resultCode = when (getUrlState(intent.data.toString())) {
                UrlState.CANCELLED -> AppCompatActivity.RESULT_CANCELED
                UrlState.ERROR -> WebViewActivity.RESULT_ERROR
                else -> AppCompatActivity.RESULT_OK
            }
            handleResult(resultCode, intent)
        } catch (ignored: UnsupportedOperationException) {
            handleResult(AppCompatActivity.RESULT_CANCELED, intent)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    open fun canCaptureUrl(url: String?): Boolean {
        val captureUrl = getCaptureUrl(returnUrl)
        return captureUrl?.let { url?.startsWith(it) } == true
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun getIntentFromUri(uri: Uri?) =
        uri?.let { uri ->
            if (uri.scheme == INTENT_SCHEMA) {
                Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
            } else {
                Intent(Intent.ACTION_VIEW).apply { data = uri }
            }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    @Suppress("SwallowedException") // exception is not being swallowed
    private fun handleIntentOnAndroid11OrAbove(intent: Intent) {
        try {
            intent.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER
            }
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "handle intent error: $e")
            cannotHandleIntent(intent)
        }
    }

    @Suppress("SwallowedException") // exception is not being swallowed
    @SuppressLint("QueryPermissionsNeeded")
    private fun handleIntentOnAndroid10OrBelow(intent: Intent) {
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        } else {
            Log.e(TAG, "intent.resolveActivity(packageManager) is null")
            cannotHandleIntent(intent)
        }
    }

    private fun canAnyAppHandleUrl(uri: Uri?): Boolean {
        val intent = getIntentFromUri(uri)
        val apps = intent?.let {
            activity.packageManager.queryIntentActivities(intent, 0)
                .map { it.activityInfo.packageName }.toSet()
        } ?: emptySet()
        return apps.minus(browserApps).isEmpty().not()
    }

    /**
     * 1. In case return url is same as url, we need to trigger error.
     * 2. In case there is an HTTP POST redirect, we won't enter @see [shouldOverrideUrlLoading].
     * We will try to handle the deeplink in that case for ERROR_UNSUPPORTED_SCHEME.
     */
    private fun handleError(requestUrl: String?, errorCode: Int?) {
        when {
            requestUrl == url -> cannotHandleIntent(Intent(requestUrl))
            errorCode == ERROR_UNSUPPORTED_SCHEME && canCaptureUrl(requestUrl)
            -> handleDeepLink(Uri.parse(requestUrl))
        }
    }

    enum class UrlState {
        CANCELLED,
        ERROR,
        PROCESSING,
        SUCCESS
    }

    protected companion object {

        const val TAG: String = "BaseWebViewClient"
        const val INTENT_SCHEMA = "intent"
        const val SAMPLE_URL = "https://primer.io"
    }
}
