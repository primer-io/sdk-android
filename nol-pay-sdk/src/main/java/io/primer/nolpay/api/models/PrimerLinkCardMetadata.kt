package io.primer.nolpay.api.models

import java.io.Serializable

data class PrimerLinkCardMetadata(val linkToken: String, val cardNumber: String) : Serializable {
    private companion object {
        private const val serialVersionUID: Long = 1L
    }
}
