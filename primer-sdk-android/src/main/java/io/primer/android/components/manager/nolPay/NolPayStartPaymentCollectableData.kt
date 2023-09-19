package io.primer.android.components.manager.nolPay

import android.nfc.Tag
import io.primer.android.components.manager.nolPay.core.composable.NolPayCollectableData
import io.primer.nolpay.models.PrimerNolPaymentCard

sealed interface NolPayStartPaymentCollectableData : NolPayCollectableData {

    data class NolPayStartPaymentData(
        val nolPaymentCard: PrimerNolPaymentCard,
        val mobileNumber: String,
        val phoneCountryDiallingCode: String
    ) : NolPayStartPaymentCollectableData

    data class NolPayTagData(val tag: Tag) : NolPayStartPaymentCollectableData
}
