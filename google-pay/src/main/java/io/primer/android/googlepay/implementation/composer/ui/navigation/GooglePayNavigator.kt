package io.primer.android.googlepay.implementation.composer.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartActivityForResultNavigator
import io.primer.android.googlepay.GooglePayFacadeFactory
import io.primer.android.googlepay.implementation.composer.ui.navigation.launcher.GooglePayActivityLauncherParams

internal data class GooglePayNavigator(
    private val activity: Activity,
    private val logReporter: LogReporter,
    override val launcher: ActivityResultLauncher<Intent>,
    private val facadeFactory: GooglePayFacadeFactory
) : StartActivityForResultNavigator<GooglePayActivityLauncherParams>(launcher) {

    override fun navigate(params: GooglePayActivityLauncherParams) {
        facadeFactory.create(activity, params.environment, logReporter)
            .pay(
                activity = activity,
                gatewayMerchantId = params.gatewayMerchantId,
                merchantName = params.merchantName,
                totalPrice = params.totalPrice,
                countryCode = params.countryCode,
                currencyCode = params.currencyCode,
                allowedCardNetworks = params.allowedCardNetworks,
                allowedCardAuthMethods = params.allowedCardAuthMethods,
                billingAddressRequired = params.billingAddressRequired,
                shippingOptions = params.shippingOptions,
                shippingAddressParameters = params.shippingAddressParameters,
                emailAddressRequired = params.emailAddressRequired,
                requireShippingMethod = params.requireShippingMethod
            )
    }

    override fun canHandle(params: NavigationParams): Boolean {
        return params is GooglePayActivityLauncherParams
    }
}
