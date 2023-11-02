package io.primer.android.components.manager.nolPay.payment.composable

import android.nfc.Tag
import io.primer.android.components.manager.nolPay.core.composable.NolPayCollectableData
import io.primer.nolpay.api.models.PrimerNolPaymentCard

/**
 * A sealed interface representing collectable data needed for payment with Nol Pay card.
 * This data includes information related to the payment process.
 */
sealed interface NolPayPaymentCollectableData : NolPayCollectableData {

    /**
     * Data class representing Nol Pay card and phone data used in payment process.
     *
     * @property nolPaymentCard The NolPay payment card to unlink.
     * @property mobileNumber The mobile number associated with the NolPay account.
     * E.164 format.
     * */
    data class NolPayCardAndPhoneData(
        val nolPaymentCard: PrimerNolPaymentCard,
        val mobileNumber: String
    ) : NolPayPaymentCollectableData

    /**
     * Data class representing Nol Pay tag data collected during the payment process.
     *
     * @property tag The tag associated with the Nol Pay card.
     */
    data class NolPayTagData(val tag: Tag) : NolPayPaymentCollectableData
}
