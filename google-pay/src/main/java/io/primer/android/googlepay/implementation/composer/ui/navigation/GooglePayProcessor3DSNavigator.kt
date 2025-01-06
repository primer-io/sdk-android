package io.primer.android.googlepay.implementation.composer.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.android.processor3ds.ui.Processor3dsWebViewActivity
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartActivityForResultNavigator

internal data class GooglePayProcessor3DSNavigator(
    private val context: Activity,
    override val launcher: ActivityResultLauncher<Intent>,
) : StartActivityForResultNavigator<GooglePayProcessor3DSActivityLauncherParams>(launcher) {
    override fun navigate(params: GooglePayProcessor3DSActivityLauncherParams) {
        launcher.launch(
            Processor3dsWebViewActivity.getLaunchIntent(
                context = context,
                paymentUrl = params.redirectUrl,
                statusUrl = params.statusUrl,
                title = params.title,
                paymentMethodType = params.paymentMethodType,
            ),
        )
    }

    override fun canHandle(params: NavigationParams): Boolean {
        return params is GooglePayProcessor3DSActivityLauncherParams
    }
}
