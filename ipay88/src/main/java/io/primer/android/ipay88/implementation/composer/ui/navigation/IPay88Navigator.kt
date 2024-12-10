package io.primer.android.ipay88.implementation.composer.ui.navigation

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.primer.android.ipay88.implementation.composer.ui.navigation.extension.toIPay88LauncherParams
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartActivityForResultNavigator
import io.primer.android.ipay88.implementation.composer.ui.navigation.launcher.IPay88ActivityLauncherParams
import io.primer.ipay88.api.ui.NativeIPay88Activity

internal data class IPay88Navigator(
    private val context: Context,
    override val launcher: ActivityResultLauncher<Intent>
) : StartActivityForResultNavigator<IPay88ActivityLauncherParams>(launcher) {

    override fun navigate(params: IPay88ActivityLauncherParams) {
        launcher.launch(
            NativeIPay88Activity.getLaunchIntent(
                context = context,
                params = params.toIPay88LauncherParams()
            )
        )
    }

    override fun canHandle(params: NavigationParams): Boolean {
        return params is IPay88ActivityLauncherParams
    }
}
