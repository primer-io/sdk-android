package io.primer.ipay88.internal.extension

import com.ipay.IPayIHPayment
import io.primer.ipay88.api.ui.IPay88LauncherParams

internal fun IPay88LauncherParams.toIPayIHPayment() = IPayIHPayment().let {
    it.amount = amount
    it.refNo = referenceNumber
    it.paymentId = iPayPaymentId
    it.fixpaymentid = iPayPaymentId
    it.userName = customerName
    it.userEmail = customerEmail
    it.currency = currencyCode
    it.country = countryCode
    it.merchantCode = merchantCode
    it.prodDesc = prodDesc
    it.actionType = actionType
    it.remark = remark
    it.appdeeplink = deeplinkUrl
    it.backendPostURL = backendCallbackUrl
    it
}
