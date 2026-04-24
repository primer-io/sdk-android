package io.primer.checkout.orchestrator.data.model

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer

internal data class ManifestResponse(
    val signature: String,
    val manifest: String,
) : JSONDeserializable {

    companion object {
        private const val SIGNATURE_FIELD = "signature"
        private const val MANIFEST_FIELD = "manifest"

        @JvmField
        val deserializer = JSONObjectDeserializer { json ->
            val signature = json.optString(SIGNATURE_FIELD)
            val manifest = json.optString(MANIFEST_FIELD)

            check(signature.isNotBlank() && manifest.isNotBlank()) {
                "Invalid manifest response format"
            }
            ManifestResponse(
                signature = signature,
                manifest = manifest,
            )
        }
    }
}
