package io.primer.android.components.ui.navigation

import android.content.Context
import android.content.Intent
import io.primer.android.threeds.ui.ThreeDsActivity
import io.primer.android.ui.base.webview.WebViewClientType
import io.primer.android.ui.payment.async.AsyncPaymentMethodWebViewActivity

internal class Navigator(private val context: Context) {

    fun openThreeDsScreen() {
        context.startActivity(
            ThreeDsActivity.getLaunchIntent(context).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    fun openAsyncWebViewScreen(title: String, redirectUrl: String, statusUrl: String) {
        context.startActivity(
            AsyncPaymentMethodWebViewActivity.getLaunchIntent(
                context,
                redirectUrl,
                "",
                statusUrl,
                title,
                WebViewClientType.ASYNC
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }
}
