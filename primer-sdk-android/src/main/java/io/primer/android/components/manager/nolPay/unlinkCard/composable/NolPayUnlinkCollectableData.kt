package io.primer.android.components.manager.nolPay.unlinkCard.composable

import io.primer.android.components.manager.nolPay.core.composable.NolPayCollectableData
import io.primer.nolpay.api.models.PrimerNolPaymentCard

/**
 * A sealed interface representing different types of data that can be collected
 * for unlinking a Nol Pay card.
 */
sealed interface NolPayUnlinkCollectableData : NolPayCollectableData {

    /**
     * Data class representing Nol Pay initial data for unlinking.
     *
     * @property nolPaymentCard The NolPay payment card to unlink.
     * @property mobileNumber The mobile number associated with the NolPay account
     * in E.164 format.
     */
    data class NolPayCardAndPhoneData(
        val nolPaymentCard: PrimerNolPaymentCard,
        val mobileNumber: String
    ) : NolPayUnlinkCollectableData

    /**
     * Data class representing Nol Pay OTP (One-Time Password) data for unlinking.
     *
     * @property otpCode The OTP code used for unlinking.
     */
    data class NolPayOtpData(val otpCode: String) : NolPayUnlinkCollectableData
}
