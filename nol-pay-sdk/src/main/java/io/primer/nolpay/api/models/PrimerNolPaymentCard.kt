package io.primer.nolpay.api.models

import java.io.Serializable

data class PrimerNolPaymentCard(val cardNumber: String) : Serializable {
    private companion object {
        private const val serialVersionUID: Long = 1L
    }
}
