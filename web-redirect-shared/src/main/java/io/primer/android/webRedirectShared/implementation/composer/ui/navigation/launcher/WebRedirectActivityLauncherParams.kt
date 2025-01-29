package io.primer.android.webRedirectShared.implementation.composer.ui.navigation.launcher

import io.primer.android.PrimerSessionIntent
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodRedirectLauncherParams

data class WebRedirectActivityLauncherParams(
    val statusUrl: String,
    val paymentUrl: String,
    val title: String,
    override val paymentMethodType: String,
    val returnUrl: String,
) : PaymentMethodRedirectLauncherParams(
    paymentMethodType,
    PrimerSessionIntent.CHECKOUT,
)
