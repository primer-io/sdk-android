package io.primer.android.paypal.implementation.composer.ui.navigation.launcher

import io.primer.android.PrimerSessionIntent
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodRedirectLauncherParams

internal data class BrowserLauncherParams(
    val url: String,
    val host: String,
    override val paymentMethodType: String,
    override val sessionIntent: PrimerSessionIntent
) : PaymentMethodRedirectLauncherParams(
    paymentMethodType,
    sessionIntent
)
