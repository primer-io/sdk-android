package io.primer.sample.test

import io.primer.nolpay.models.PrimerNolPaymentCard

interface PrimerHeadlessUiState


sealed interface NolPayUiLinkState {
    object SubmittingTagData
    object SubmittingPhoneData
    object SubmittingOtpData
    data class CardLinked(val nolPaymentCard: PrimerNolPaymentCard)
}

sealed interface PrimerHeadlessStatable {

}