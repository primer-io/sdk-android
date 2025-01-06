package io.primer.android.webRedirectShared.implementation.composer.ui.navigation.handler

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.android.webRedirectShared.implementation.composer.ui.navigation.WebRedirectNavigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.Navigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.PaymentMethodContextNavigationHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartNewTaskNavigator

internal class WebRedirectNavigationHandler : PaymentMethodContextNavigationHandler {
    override fun getSupportedNavigators(context: Context): List<Navigator<NavigationParams>> {
        return listOf(StartNewTaskNavigator(context))
    }

    override fun getSupportedNavigators(
        activity: Activity,
        launcher: ActivityResultLauncher<Intent>,
    ): List<Navigator<NavigationParams>> {
        return listOf(WebRedirectNavigator(activity, launcher))
    }
}
