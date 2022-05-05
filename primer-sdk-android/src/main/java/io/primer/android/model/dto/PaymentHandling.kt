package io.primer.android.model.dto

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentHandling {
    AUTO,
    MANUAL
}
