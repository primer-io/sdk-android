package io.primer.android.model.dto

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class PaymentMethodRemoteConfig(
    val id: String? = null, // FIXME how come id can be null but not the other fields?
    val type: String, // FIXME what is this?
    val options: PaymentMethodRemoteConfigOptions? = null
)

@Keep
@Serializable
data class PaymentMethodRemoteConfigOptions(
    val merchantId: String? = null
)
