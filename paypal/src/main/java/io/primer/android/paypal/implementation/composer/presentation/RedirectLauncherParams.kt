package io.primer.android.paypal.implementation.composer.presentation

import io.primer.android.PrimerSessionIntent
import io.primer.android.paymentmethods.common.InitialLauncherParams

internal data class RedirectLauncherParams(
    val url: String,
    val successUrl: String,
    val paymentMethodConfigId: String,
    val paymentMethodType: String,
    val sessionIntent: PrimerSessionIntent,
) : InitialLauncherParams
