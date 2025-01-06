package io.primer.android.googlepay.implementation.composer.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.primer.android.PrimerSessionIntent
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.android.threeds.ui.ThreeDsActivity
import io.primer.android.threeds.ui.launcher.ThreeDsActivityLauncherParams
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartActivityForResultNavigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodRedirectLauncherParams

internal data class GooglePayNative3DSActivityLauncherParams(
    override val paymentMethodType: String,
    val supportedThreeDsVersions: List<String>,
) : PaymentMethodRedirectLauncherParams(
        paymentMethodType,
        PrimerSessionIntent.CHECKOUT,
    )

internal data class GooglePayProcessor3DSActivityLauncherParams(
    override val paymentMethodType: String,
    val redirectUrl: String,
    val statusUrl: String,
    val title: String,
) : PaymentMethodRedirectLauncherParams(
        paymentMethodType,
        PrimerSessionIntent.CHECKOUT,
    )

internal data class GooglePay3DSNavigator(
    private val context: Activity,
    override val launcher: ActivityResultLauncher<Intent>,
) : StartActivityForResultNavigator<GooglePayNative3DSActivityLauncherParams>(launcher) {
    override fun navigate(params: GooglePayNative3DSActivityLauncherParams) {
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
        return params is GooglePayNative3DSActivityLauncherParams
    }
}
