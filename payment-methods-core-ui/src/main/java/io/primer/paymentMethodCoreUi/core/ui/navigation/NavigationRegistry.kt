package io.primer.paymentMethodCoreUi.core.ui.navigation

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.paymentMethodCoreUi.core.ui.HeadlessActivity

abstract class StartActivityForResultNavigator<T : NavigationParams>(
    open val launcher: ActivityResultLauncher<Intent>
) : Navigator<T>

abstract class StartActivityNavigator<T : NavigationParams>(
    open val context: Context
) : Navigator<T>

class StartNewTaskNavigator(
    override val context: Context
) : StartActivityNavigator<PaymentMethodLauncherParams>(context) {
    override fun navigate(params: PaymentMethodLauncherParams) {
        context.startActivity(
            HeadlessActivity.getLaunchIntent(context, params).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    override fun canHandle(params: NavigationParams): Boolean {
        return params is PaymentMethodLauncherParams
    }
}
