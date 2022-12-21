package io.primer.android.components.ui.navigation

import android.content.Context
import android.content.Intent
import io.primer.android.components.ui.activity.HeadlessActivity
import io.primer.android.threeds.ui.ThreeDsActivity
import io.primer.android.ui.base.webview.WebViewClientType
import io.primer.android.ui.payment.async.AsyncPaymentMethodWebViewActivity
import io.primer.android.ui.payment.processor3ds.Processor3dsWebViewActivity
import io.primer.ipay88.api.ui.IPay88LauncherParams

internal class Navigator(private val context: Context) {

    fun openThreeDsScreen() {
        context.startActivity(
            ThreeDsActivity.getLaunchIntent(context).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    fun openAsyncWebViewScreen(
        title: String,
        paymentMethodType: String,
        redirectUrl: String,
        deeplinkUrl: String
    ) {
        context.startActivity(
            AsyncPaymentMethodWebViewActivity.getLaunchIntent(
                context, redirectUrl, deeplinkUrl, title, paymentMethodType, WebViewClientType.ASYNC
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }

    fun openProcessor3dsViewScreen(
        title: String,
        paymentMethodType: String,
        redirectUrl: String,
        statusUrl: String
    ) {
        context.startActivity(
            Processor3dsWebViewActivity.getLaunchIntent(
                context,
                redirectUrl,
                "",
                statusUrl,
                title,
                paymentMethodType,
                WebViewClientType.PROCESSOR_3DS
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }

    fun openHeadlessScreen(params: IPay88LauncherParams) {
        context.startActivity(
            HeadlessActivity.getLaunchIntent(
                context, params
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }
}
