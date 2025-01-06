package io.primer.android.paypal.implementation.composer.ui.navigation

import android.app.Activity
import com.example.customtabs.launchCustomTab
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.android.paypal.implementation.composer.ui.navigation.launcher.BrowserLauncherParams
import io.primer.paymentMethodCoreUi.core.ui.HeadlessActivity
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartActivityNavigator

internal data class PaypalNavigator(
    private val activity: Activity,
) : StartActivityNavigator<BrowserLauncherParams>(activity) {
    override fun navigate(params: BrowserLauncherParams) {
        activity.apply {
            intent.putExtra(HeadlessActivity.LAUNCHED_BROWSER_KEY, true)
            launchCustomTab(params.url)
        }
    }

    override fun canHandle(params: NavigationParams): Boolean {
        return params is BrowserLauncherParams
    }
}
