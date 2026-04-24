package io.primer.jscore.domain.core.models

internal sealed class JsResource {
    data class Js(val content: String) : JsResource()

    data class Wasm(val bytes: ByteArray, val name: String) : JsResource()
}
