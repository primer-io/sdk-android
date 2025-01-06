package io.primer.paymentMethodCoreUi.core.ui.composable

import android.content.Intent
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams

fun interface ActivityResultIntentHandler {
    fun handleActivityResultIntent(
        params: PaymentMethodLauncherParams,
        resultCode: Int,
        intent: Intent?,
    )
}
