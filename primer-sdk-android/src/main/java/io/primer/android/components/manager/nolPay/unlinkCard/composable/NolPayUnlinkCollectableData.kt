package io.primer.android.components.manager.nolPay.unlinkCard.composable

import io.primer.android.components.manager.nolPay.core.composable.NolPayCollectableData
import io.primer.nolpay.models.PrimerNolPaymentCard

sealed interface NolPayUnlinkCollectableData : NolPayCollectableData {

    data class NolPayCardData(val nolPaymentCard: PrimerNolPaymentCard) :
        NolPayUnlinkCollectableData

    data class NolPayPhoneData(val mobileNumber: String, val phoneCountryDiallingCode: String) :
        NolPayUnlinkCollectableData

    data class NolPayOtpData(val otpCode: String) : NolPayUnlinkCollectableData
}
