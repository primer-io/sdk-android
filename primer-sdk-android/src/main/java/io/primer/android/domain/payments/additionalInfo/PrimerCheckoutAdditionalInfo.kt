package io.primer.android.domain.payments.additionalInfo

import androidx.activity.result.ActivityResultRegistry

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

sealed interface AchAdditionalInfo : PrimerCheckoutAdditionalInfo {
    data class ProvideActivityResultRegistry(
        /**
         * Provides an [ActivityResultRegistry] to be used for ACH bank account selection.
         */
        val provide: (ActivityResultRegistry) -> Unit
    ) : AchAdditionalInfo

    data class DisplayMandate(
        /**
         * Accepts the ACH mandate, completing the payment.
         */
        val onAcceptMandate: suspend () -> Unit,
        /**
         * Declines the ACH mandate, cancelling the payment.
         */
        val onDeclineMandate: suspend () -> Unit
    ) : AchAdditionalInfo
}
