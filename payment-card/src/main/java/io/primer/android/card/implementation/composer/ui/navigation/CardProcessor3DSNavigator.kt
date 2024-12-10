package io.primer.android.card.implementation.composer.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.primer.android.PrimerSessionIntent
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.android.processor3ds.ui.Processor3dsWebViewActivity
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartActivityForResultNavigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodRedirectLauncherParams

internal data class CardProcessor3DSActivityLauncherParams(
    override val paymentMethodType: String,
    val redirectUrl: String,
    val statusUrl: String,
    val title: String
) : PaymentMethodRedirectLauncherParams(
    paymentMethodType,
    PrimerSessionIntent.CHECKOUT
)

internal data class CardProcessor3DSNavigator(
    private val context: Activity,
    override val launcher: ActivityResultLauncher<Intent>
) : StartActivityForResultNavigator<CardProcessor3DSActivityLauncherParams>(launcher) {

    override fun navigate(params: CardProcessor3DSActivityLauncherParams) {
        launcher.launch(
            Processor3dsWebViewActivity.getLaunchIntent(
                context = context,
                paymentUrl = params.redirectUrl,
                statusUrl = params.statusUrl,
                title = params.title,
                paymentMethodType = params.paymentMethodType
            )
        )
    }

    override fun canHandle(params: NavigationParams): Boolean {
        return params is CardProcessor3DSActivityLauncherParams
    }
}
