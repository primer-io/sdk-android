package io.primer.android.paypal.implementation.composer.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.example.customtabs.canLaunchCustomTabs
import com.example.customtabs.launchCustomTab
import com.example.customtabs.openInBrowser
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.android.paypal.implementation.composer.ui.navigation.launcher.BrowserLauncherParams
import io.primer.paymentMethodCoreUi.core.ui.HeadlessActivity
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartActivityForResultNavigator

internal data class PaypalNavigator(
    private val activity: Activity,
    override val launcher: ActivityResultLauncher<Intent>,
) : StartActivityForResultNavigator<BrowserLauncherParams>(launcher) {
    override fun navigate(params: BrowserLauncherParams) {
        activity.apply {
            if (canLaunchCustomTabs()) {
                launchCustomTab(launcher = launcher, url = params.url)
            } else {
                intent.putExtra(HeadlessActivity.LAUNCHED_BROWSER_KEY, true)
                openInBrowser(url = params.url)
            }
        }
    }

    override fun canHandle(params: NavigationParams): Boolean {
        return params is BrowserLauncherParams
    }
}
