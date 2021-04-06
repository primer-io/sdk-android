package io.primer.android.model.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.util.Collections

@Serializable
internal data class PaymentMethodRemoteConfig(
    val id: String? = null, // FIXME how come id can be null but not the other fields?
    val type: String, // FIXME what is this?

    // FIXME why's there a JsonObject here?
    val options: JsonObject = JsonObject(content = Collections.emptyMap()),
)
