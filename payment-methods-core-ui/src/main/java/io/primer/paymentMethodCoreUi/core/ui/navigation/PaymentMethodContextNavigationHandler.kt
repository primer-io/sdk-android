package io.primer.paymentMethodCoreUi.core.ui.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandler

interface PaymentMethodContextNavigationHandler : PaymentMethodNavigationHandler {

    fun getSupportedNavigators(context: Context): List<Navigator<NavigationParams>>

    fun getSupportedNavigators(
        activity: Activity,
        launcher: ActivityResultLauncher<Intent>
    ): List<Navigator<NavigationParams>>
}
