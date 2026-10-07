package io.primer.ipay88.api.ui

import java.io.Serializable

data class IPay88LauncherParams(
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
    val errorCode: Int
) : Serializable
