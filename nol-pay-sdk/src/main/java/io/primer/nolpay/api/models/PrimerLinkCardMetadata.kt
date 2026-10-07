package io.primer.nolpay.api.models

import java.io.Serializable

data class PrimerLinkCardMetadata(val linkToken: String, val cardNumber: String) : Serializable
