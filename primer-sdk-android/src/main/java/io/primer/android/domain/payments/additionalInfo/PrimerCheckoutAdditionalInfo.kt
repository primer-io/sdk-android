package io.primer.android.domain.payments.additionalInfo

interface PrimerCheckoutAdditionalInfo

interface PrimerCheckoutQRCodeInfo : PrimerCheckoutAdditionalInfo

interface PrimerCheckoutBarcodeInfo : PrimerCheckoutAdditionalInfo

data class MultibancoCheckoutAdditionalInfo(
    val expiresAt: String,
    val reference: String,
    val entity: String,
) : PrimerCheckoutAdditionalInfo

data class PromptPayCheckoutAdditionalInfo(
    val expiresAt: String,
    val qrCodeUrl: String?,
    val qrCodeBase64: String?,
) : PrimerCheckoutQRCodeInfo

data class RetailOutletsCheckoutAdditionalInfo(
    val expiresAt: String,
    val couponCode: String,
    val retailerName: String?,
) : PrimerCheckoutBarcodeInfo
