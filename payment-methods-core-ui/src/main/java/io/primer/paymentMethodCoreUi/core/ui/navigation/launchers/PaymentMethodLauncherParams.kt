package io.primer.paymentMethodCoreUi.core.ui.navigation.launchers

import io.primer.android.PrimerSessionIntent
import io.primer.android.paymentmethods.common.InitialLauncherParams

data class PaymentMethodLauncherParams(
    override val paymentMethodType: String,
    override val sessionIntent: PrimerSessionIntent,
    val initialLauncherParams: InitialLauncherParams? = null,
) : ActivityLauncherParams(
        paymentMethodType,
        sessionIntent,
    )
