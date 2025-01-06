package io.primer.android.webRedirectShared.implementation.composer.ui.navigation

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.android.webRedirectShared.implementation.composer.ui.activity.WebRedirectActivity
import io.primer.android.webRedirectShared.implementation.composer.ui.navigation.launcher.WebRedirectActivityLauncherParams
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartActivityForResultNavigator

internal data class WebRedirectNavigator(
    private val context: Context,
    override val launcher: ActivityResultLauncher<Intent>,
) : StartActivityForResultNavigator<WebRedirectActivityLauncherParams>(launcher) {
    override fun navigate(params: WebRedirectActivityLauncherParams) {
        launcher.launch(
            WebRedirectActivity.getLaunchIntent(
                context = context,
                paymentUrl = params.paymentUrl,
                deeplinkUrl = params.returnUrl,
                title = params.title,
                paymentMethodType = params.paymentMethodType,
            ),
        )
    }

    override fun canHandle(params: NavigationParams): Boolean {
        return params is WebRedirectActivityLauncherParams
    }
}
