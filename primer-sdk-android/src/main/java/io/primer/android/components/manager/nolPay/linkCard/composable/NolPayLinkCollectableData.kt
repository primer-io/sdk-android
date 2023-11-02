package io.primer.android.components.manager.nolPay.linkCard.composable

import android.nfc.Tag
import io.primer.android.components.manager.nolPay.core.composable.NolPayCollectableData

/**
 * A sealed interface representing collectable data for linking a Nol Pay card.
 * This data includes information related to the linking process.
 */
sealed interface NolPayLinkCollectableData : NolPayCollectableData {

    /**
     * Data class representing Nol Pay tag data collected during the linking process.
     *
     * @property tag The tag associated with the Nol Pay card.
     */
    data class NolPayTagData(val tag: Tag) : NolPayLinkCollectableData

    /**
     * Data class representing Nol Pay phone data collected during the linking process.
     *
     * @property mobileNumber The mobile number associated with the Nol Pay payment card
     * in E.164 format.
     */
    data class NolPayPhoneData(
        val mobileNumber: String
    ) : NolPayLinkCollectableData

    /**
     * Data class representing Nol Pay OTP (One-Time Password) data collected during the linking process.
     *
     * @property otpCode The OTP code required for card linking.
     */
    data class NolPayOtpData(val otpCode: String) : NolPayLinkCollectableData
}
