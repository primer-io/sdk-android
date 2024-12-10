package io.primer.paymentMethodCoreUi.core.ui.composable

import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams

fun interface ActivityStartIntentHandler {

    fun handleActivityStartEvent(params: PaymentMethodLauncherParams)
}
