package io.primer.nolpay.api.models

import java.io.Serializable

data class PrimerUnlinkCardMetadata(val unlinkToken: String, val cardNumber: String) : Serializable
