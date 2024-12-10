package io.primer.android.nolpay.api.manager.linkCard.composable

import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStep
import io.primer.nolpay.api.models.PrimerNolPaymentCard

/**
 * A sealed interface representing the steps involved in linking a Nol Pay card.
 */
sealed interface NolPayLinkCardStep : PrimerHeadlessStep {

    /**
     * Object representing the step of collecting tag data for Nol Pay card linking.
     */
    object CollectTagData : NolPayLinkCardStep

    /**
     * A data class representing the step of collecting phone data for Nol Pay card linking.
     *
     * @property cardNumber The card number associated with the Nol Pay card.
     */
    data class CollectPhoneData(val cardNumber: String) : NolPayLinkCardStep

    /**
     * A data class representing the step of collecting OTP (One-Time Password) data
     * for Nol Pay card linking.
     * @property mobileNumber The mobile number to which OTP code was sent to.
     */
    data class CollectOtpData(val mobileNumber: String) : NolPayLinkCardStep

    /**
     * A data class representing the step of successfully linking a Nol Pay card.
     *
     * @property nolPaymentCard The linked Nol Pay card.
     */
    data class CardLinked(val nolPaymentCard: PrimerNolPaymentCard) : NolPayLinkCardStep
}
