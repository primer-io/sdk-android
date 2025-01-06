package io.primer.android.ipay88.implementation.composer.presentation

import io.primer.android.PrimerSessionIntent
import io.primer.android.paymentmethods.common.InitialLauncherParams

internal data class RedirectLauncherParams(
    val statusUrl: String,
    val iPayPaymentId: String,
    val iPayMethod: Int,
    val merchantCode: String,
    val actionType: String?,
    val amount: String,
    val referenceNumber: String,
    val prodDesc: String,
    val currencyCode: String?,
    val countryCode: String?,
    val customerName: String?,
    val customerEmail: String?,
    val remark: String?,
    val backendCallbackUrl: String,
    val deeplinkUrl: String,
    val errorCode: Int,
    val paymentMethodType: String,
    val sessionIntent: PrimerSessionIntent,
) : InitialLauncherParams
