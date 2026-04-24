package io.primer.checkout.orchestrator.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.paymentMethodCoreUi.core.ui.navigation.Navigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.PaymentMethodContextNavigationHandler

internal class BackendDrivenNavigationHandler(
    private val handlers: List<PaymentMethodContextNavigationHandler>,
) : PaymentMethodContextNavigationHandler {
    override fun getSupportedNavigators(context: Context): List<Navigator<NavigationParams>> =
        handlers.flatMap { it.getSupportedNavigators(context) }

    override fun getSupportedNavigators(
        activity: Activity,
        launcher: ActivityResultLauncher<Intent>,
    ): List<Navigator<NavigationParams>> =
        handlers.flatMap { it.getSupportedNavigators(activity, launcher) }
}
