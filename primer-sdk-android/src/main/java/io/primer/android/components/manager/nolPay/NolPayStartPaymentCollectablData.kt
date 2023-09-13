package io.primer.android.components.manager.nolPay

import android.nfc.Tag
import io.primer.nolpay.models.PrimerNolPaymentCard

sealed interface NolPayStartPaymentCollectableData : NolPayCollectableData {
    data class NolPayCardData(val nolPaymentCard: PrimerNolPaymentCard) :
        NolPayStartPaymentCollectableData

    data class NolPayTagData(val tag: Tag) : NolPayStartPaymentCollectableData
}
