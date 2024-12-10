package io.primer.android.ipay88.implementation.composer.ui.navigation.launcher

import io.primer.android.PrimerSessionIntent
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodRedirectLauncherParams

internal class IPay88ActivityLauncherParams(
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
    override val paymentMethodType: String,
    override val sessionIntent: PrimerSessionIntent
) : PaymentMethodRedirectLauncherParams(paymentMethodType, sessionIntent)
