package io.primer.android.domain.payments.additionalInfo

interface PrimerCheckoutAdditionalInfo

interface PrimerCheckoutQRCodeInfo : PrimerCheckoutAdditionalInfo

interface PrimerCheckoutVoucherAdditionalInfo : PrimerCheckoutAdditionalInfo

data class MultibancoCheckoutAdditionalInfo(
    val expiresAt: String,
    val reference: String,
    val entity: String
) : PrimerCheckoutAdditionalInfo

data class PromptPayCheckoutAdditionalInfo(
    val expiresAt: String,
    val qrCodeUrl: String?,
    val qrCodeBase64: String?
) : PrimerCheckoutQRCodeInfo

data class XenditCheckoutVoucherAdditionalInfo(
    val expiresAt: String,
    val couponCode: String,
    val retailerName: String?
) : PrimerCheckoutVoucherAdditionalInfo
