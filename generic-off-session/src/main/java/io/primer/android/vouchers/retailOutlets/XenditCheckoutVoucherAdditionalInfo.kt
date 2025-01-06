package io.primer.android.vouchers.retailOutlets

import io.primer.android.PrimerCheckoutVoucherAdditionalInfo

data class XenditCheckoutVoucherAdditionalInfo(
    val expiresAt: String,
    val couponCode: String,
    val retailerName: String?,
) : PrimerCheckoutVoucherAdditionalInfo {
    override val completesCheckout: Boolean = true
}
