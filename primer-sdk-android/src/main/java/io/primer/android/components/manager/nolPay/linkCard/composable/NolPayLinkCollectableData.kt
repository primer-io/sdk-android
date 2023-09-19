package io.primer.android.components.manager.nolPay.linkCard.composable

import android.nfc.Tag
import io.primer.android.components.manager.nolPay.core.composable.NolPayCollectableData

sealed interface NolPayLinkCollectableData : NolPayCollectableData {

    data class NolPayTagData(val tag: Tag) : NolPayLinkCollectableData

    data class NolPayPhoneData(val mobileNumber: String, val phoneCountryDiallingCode: String) :
        NolPayLinkCollectableData

    data class NolPayOtpData(val otpCode: String) : NolPayLinkCollectableData
}
