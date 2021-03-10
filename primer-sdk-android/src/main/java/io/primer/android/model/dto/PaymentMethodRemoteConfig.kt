package io.primer.android.model.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.util.*

@Serializable
internal data class PaymentMethodRemoteConfig(
    val id: String? = null,
    val type: String,
    val options: JsonObject = JsonObject(content = Collections.emptyMap()),
)
