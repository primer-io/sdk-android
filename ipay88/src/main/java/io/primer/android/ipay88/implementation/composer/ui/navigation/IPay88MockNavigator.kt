package io.primer.android.ipay88.implementation.composer.ui.navigation

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.primer.android.ipay88.implementation.composer.ui.navigation.launcher.IPay88MockActivityLauncherParams
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.paymentMethodCoreUi.core.ui.mock.PaymentMethodMockActivity
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartActivityForResultNavigator

internal data class IPay88MockNavigator(
    private val context: Context,
    override val launcher: ActivityResultLauncher<Intent>
) : StartActivityForResultNavigator<IPay88MockActivityLauncherParams>(launcher) {

    override fun navigate(params: IPay88MockActivityLauncherParams) {
        launcher.launch(
            PaymentMethodMockActivity.getLaunchIntent(context, params.paymentMethodType)
        )
    }

    override fun canHandle(params: NavigationParams): Boolean {
        return params is IPay88MockActivityLauncherParams
    }
}
