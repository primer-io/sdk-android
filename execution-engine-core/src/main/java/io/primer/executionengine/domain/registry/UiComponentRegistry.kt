package io.primer.executionengine.domain.registry

/** The UI component types this SDK can draw, by type and version. */
interface UiComponentRegistry {
    val declaredVersions: Map<String, String>

    fun declare(type: String, version: String)
}
