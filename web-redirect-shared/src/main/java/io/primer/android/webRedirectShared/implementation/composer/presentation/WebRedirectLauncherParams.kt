package io.primer.android.webRedirectShared.implementation.composer.presentation

import io.primer.android.paymentmethods.common.InitialLauncherParams

data class WebRedirectLauncherParams(
    val title: String,
    val paymentMethodType: String,
    val redirectUrl: String,
    val statusUrl: String,
    val returnUrl: String
) : InitialLauncherParams
