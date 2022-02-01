package io.primer.android.ui.base.webview

import android.annotation.SuppressLint
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri

internal abstract class BaseWebViewClient(
    private val activity: WebViewActivity,
    private val url: String?,
    private val returnUrl: String?,
) : WebViewClient() {

    private val numberOfBrowserApps by lazy {
        activity.packageManager.queryIntentActivities(
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(SAMPLE_URL)
            },
            0
        ).map { it.activityInfo.packageName }.toSet().size
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val isDeeplink = URLUtil.isNetworkUrl(request?.url?.toString().orEmpty()).not() ||
            canAnyAppHandleUrl(request)
        return if (isDeeplink) {
            handleDeepLink(request)
        } else {
            handleNetworkUrl(request)
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        super.onReceivedError(view, request, error)
        if (request?.url?.toString() == url) {
            cannotHandleIntent(Intent(request?.url?.toString()))
        }
    }

    abstract fun getUrlState(url: String): UrlState

    abstract fun getCaptureUrl(url: String?): String?

    protected open fun handleDeepLink(request: WebResourceRequest?): Boolean {
        val intent = Intent(Intent.ACTION_VIEW)
        request?.url.let { uri ->
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

    protected open fun handleIntent(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Log.d(TAG, "Android 11+, data: ${intent.data}")
            handleIntentOnAndroid11OrAbove(intent)
        } else {
            Log.d(TAG, "Android 10-, data: ${intent.data}")
            handleIntentOnAndroid10OrBelow(intent)
        }
    }

    protected open fun cannotHandleIntent(intent: Intent) {
        Log.e(TAG, "Cannot handle intent: ${intent.data}")
        activity.apply {
            setResult(WebViewActivity.RESULT_ERROR, intent)
            finish()
        }
    }

    protected open fun onUrlCaptured(intent: Intent) {
        try {
            val resultCode = when (getUrlState(intent.data.toString())) {
                UrlState.CANCELLED -> AppCompatActivity.RESULT_CANCELED
                UrlState.ERROR -> WebViewActivity.RESULT_ERROR
                else -> AppCompatActivity.RESULT_OK
            }
            handleResult(resultCode, intent)
        } catch (e: UnsupportedOperationException) {
            handleResult(AppCompatActivity.RESULT_CANCELED, intent)
        }
    }

    protected open fun canCaptureUrl(url: String?): Boolean {
        val captureUrl = getCaptureUrl(returnUrl)
        return captureUrl?.let { url.orEmpty().contains(captureUrl) } == true
    }

    protected fun getIntentFromRequest(request: WebResourceRequest?) =
        request?.url?.let { uri ->
            if (request.url.scheme == INTENT_SCHEMA) {
                Intent.parseUri(request.url.toString(), Intent.URI_INTENT_SCHEME)
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

    private fun canAnyAppHandleUrl(request: WebResourceRequest?): Boolean {
        val intent = getIntentFromRequest(request)
        val numberOfApps = intent?.let {
            activity.packageManager.queryIntentActivities(intent, 0)
                .map { it.activityInfo.packageName }.toSet().size
        }
        return numberOfApps != numberOfBrowserApps
    }

    internal enum class UrlState {
        CANCELLED,
        ERROR,
        PROCESSING,
        SUCCESS,
    }

    protected companion object {

        const val TAG: String = "BaseWebViewClient"
        const val INTENT_SCHEMA = "intent"
        const val SAMPLE_URL = "https://primer.io"
    }
}
