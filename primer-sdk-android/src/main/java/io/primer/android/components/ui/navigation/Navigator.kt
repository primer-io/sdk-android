package io.primer.android.components.ui.navigation

import android.content.Context
import android.content.Intent
import io.primer.android.components.ui.activity.HeadlessActivity
import io.primer.android.components.ui.activity.PaymentMethodLauncherParams
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.threeds.ui.ThreeDsActivity
import io.primer.android.ui.base.webview.WebViewClientType
import io.primer.android.ui.mock.PaymentMethodMockActivity
import io.primer.android.ui.payment.processor3ds.Processor3dsWebViewActivity

internal class Navigator(private val context: Context) {

    fun openThreeDsScreen() {
        context.startActivity(
            ThreeDsActivity.getLaunchIntent(context).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    fun open3DSMockScreen() {
        context.startActivity(
            PaymentMethodMockActivity.getLaunchIntent(
                context,
                PaymentMethodType.PAYMENT_CARD.name
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
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

    fun openHeadlessScreen(params: PaymentMethodLauncherParams) {
        context.startActivity(
            HeadlessActivity.getLaunchIntent(context, params).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}
