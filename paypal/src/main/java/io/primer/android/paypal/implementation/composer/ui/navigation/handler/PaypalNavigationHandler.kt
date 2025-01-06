package io.primer.android.paypal.implementation.composer.ui.navigation.handler

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.android.paypal.implementation.composer.ui.navigation.PaypalNavigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.Navigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.PaymentMethodContextNavigationHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartNewTaskNavigator

internal class PaypalNavigationHandler : PaymentMethodContextNavigationHandler {
    override fun getSupportedNavigators(context: Context): List<Navigator<NavigationParams>> {
        return listOf(StartNewTaskNavigator(context))
    }

    override fun getSupportedNavigators(
        activity: Activity,
        launcher: ActivityResultLauncher<Intent>,
    ): List<Navigator<NavigationParams>> {
        return listOf(PaypalNavigator(activity))
    }
}
