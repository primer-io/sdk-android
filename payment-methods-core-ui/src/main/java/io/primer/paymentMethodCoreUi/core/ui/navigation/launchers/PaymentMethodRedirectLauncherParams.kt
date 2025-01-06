package io.primer.paymentMethodCoreUi.core.ui.navigation.launchers

import io.primer.android.PrimerSessionIntent

open class PaymentMethodRedirectLauncherParams(
    paymentMethodType: String,
    sessionIntent: PrimerSessionIntent,
) : ActivityLauncherParams(paymentMethodType, sessionIntent)
