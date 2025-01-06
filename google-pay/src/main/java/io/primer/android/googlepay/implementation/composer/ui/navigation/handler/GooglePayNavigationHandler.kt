package io.primer.android.googlepay.implementation.composer.ui.navigation.handler

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.googlepay.implementation.composer.ui.navigation.GooglePay3DSNavigator
import io.primer.android.googlepay.implementation.composer.ui.navigation.GooglePayNavigator
import io.primer.android.googlepay.implementation.composer.ui.navigation.GooglePayProcessor3DSNavigator
import io.primer.android.googlepay.implementation.composer.ui.navigation.MockGooglePay3DSNavigator
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.paymentMethodCoreUi.core.ui.navigation.Navigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.PaymentMethodContextNavigationHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartNewTaskNavigator

internal class GooglePayNavigationHandler :
    PaymentMethodContextNavigationHandler, DISdkComponent {
    override fun getSupportedNavigators(context: Context): List<Navigator<NavigationParams>> {
        return listOf(StartNewTaskNavigator(context))
    }

    override fun getSupportedNavigators(
        activity: Activity,
        launcher: ActivityResultLauncher<Intent>,
    ): List<Navigator<NavigationParams>> {
        return listOf(
            GooglePayNavigator(
                activity = activity,
                logReporter = resolve(),
                launcher = launcher,
                facadeFactory = resolve(),
            ),
            GooglePay3DSNavigator(context = activity, launcher = launcher),
            GooglePayProcessor3DSNavigator(context = activity, launcher = launcher),
            MockGooglePay3DSNavigator(context = activity, launcher = launcher),
        )
    }
}
