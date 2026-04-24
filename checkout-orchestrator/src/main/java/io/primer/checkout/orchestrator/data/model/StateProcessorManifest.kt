package io.primer.checkout.orchestrator.data.model

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import org.json.JSONObject

/**
 * Represents a remote resource with its download URL and integrity hash.
 *
 * @property url The URL to download the resource from.
 * @property sha256 The SHA-256 hash for verifying resource integrity.
 */
internal data class ResourceInfo(
    val url: String,
    val sha256: String,
)

/**
 * Represents a WASM remote resource with its download URL, integrity hash, and compressed variant.
 *
 * @property url The URL to download the resource from.
 * @property sha256 The SHA-256 hash for verifying resource integrity.
 * @property gz The URL to the gzip-compressed variant of the resource.
 */
internal data class WasmResourceInfo(
    val url: String,
    val sha256: String,
    val gz: String,
)

/**
 * Represents the CEL (Common Expression Language) execution target resources.
 *
 * @property js The JavaScript resource for CEL evaluation.
 * @property wasm The WASM resource for CEL evaluation.
 */
internal data class CelTarget(
    val js: ResourceInfo,
    val wasm: WasmResourceInfo,
)

/**
 * Represents the manifest describing the state processor and CEL engine resources
 * required for backend-driven checkout orchestration.
 *
 * @property stateProcessor The state processor resource information.
 * @property cel The CEL engine resource information.
 */
internal data class StateProcessorManifest(
    val stateProcessor: StateProcessorInfo,
    val cel: CelInfo,
) : JSONDeserializable {

    /**
     * Describes the state processor version and its UMD bundle resource.
     *
     * @property version The version of the state processor.
     * @property umd The UMD bundle resource for the state processor.
     */
    data class StateProcessorInfo(
        val version: String,
        val umd: ResourceInfo,
    )

    /**
     * Describes the CEL engine version and its execution target resources.
     *
     * @property version The version of the CEL engine.
     * @property noModules The CEL target resources built without ES modules (for broader compatibility).
     */
    data class CelInfo(
        val version: String,
        val noModules: CelTarget,
    )

    companion object {
        @JvmField
        val deserializer = JSONObjectDeserializer { json ->
            StateProcessorManifest(
                stateProcessor = parseStateProcessor(json.getJSONObject(STATE_PROCESSOR_FIELD)),
                cel = parseCel(json.getJSONObject(CEL_FIELD)),
            )
        }

        private const val STATE_PROCESSOR_FIELD = "stateProcessor"
        private const val CEL_FIELD = "cel"
        private const val VERSION_FIELD = "version"
        private const val URL_FIELD = "url"
        private const val SHA256_FIELD = "sha256"
        private const val UMD_FIELD = "umd"
        private const val NO_MODULES_FIELD = "noModules"
        private const val JS_FIELD = "js"
        private const val WASM_FIELD = "wasm"
        private const val GZ_FIELD = "gz"

        private fun parseResourceInfo(json: JSONObject) = ResourceInfo(
            url = json.getString(URL_FIELD),
            sha256 = json.getString(SHA256_FIELD),
        )

        private fun parseWasmResourceInfo(json: JSONObject) = WasmResourceInfo(
            url = json.getString(URL_FIELD),
            sha256 = json.getString(SHA256_FIELD),
            gz = json.getString(GZ_FIELD),
        )

        internal fun parseStateProcessor(json: JSONObject) = StateProcessorInfo(
            version = json.getString(VERSION_FIELD),
            umd = parseResourceInfo(json.getJSONObject(UMD_FIELD)),
        )

        internal fun parseCel(json: JSONObject) = CelInfo(
            version = json.getString(VERSION_FIELD),
            noModules = parseCelTarget(json.getJSONObject(NO_MODULES_FIELD)),
        )

        private fun parseCelTarget(json: JSONObject) = CelTarget(
            js = parseResourceInfo(json.getJSONObject(JS_FIELD)),
            wasm = parseWasmResourceInfo(json.getJSONObject(WASM_FIELD)),
        )
    }
}
