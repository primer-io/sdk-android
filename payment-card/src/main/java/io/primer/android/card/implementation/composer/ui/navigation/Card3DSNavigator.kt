package io.primer.android.card.implementation.composer.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.primer.android.PrimerSessionIntent
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.android.threeds.ui.ThreeDsActivity
import io.primer.android.threeds.ui.launcher.ThreeDsActivityLauncherParams
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartActivityForResultNavigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodRedirectLauncherParams

internal data class CardNative3DSActivityLauncherParams(
    override val paymentMethodType: String,
    val supportedThreeDsVersions: List<String>,
) : PaymentMethodRedirectLauncherParams(
        paymentMethodType,
        PrimerSessionIntent.CHECKOUT,
    )

internal data class Card3DSNavigator(
    private val context: Activity,
    override val launcher: ActivityResultLauncher<Intent>,
) : StartActivityForResultNavigator<CardNative3DSActivityLauncherParams>(launcher) {
    override fun navigate(params: CardNative3DSActivityLauncherParams) {
        launcher.launch(
            ThreeDsActivity.getLaunchIntent(
                context = context,
                params =
                    ThreeDsActivityLauncherParams(
                        supportedThreeDsProtocolVersions = params.supportedThreeDsVersions,
                    ),
            ),
        )
    }

    override fun canHandle(params: NavigationParams): Boolean {
        return params is CardNative3DSActivityLauncherParams
    }
}
