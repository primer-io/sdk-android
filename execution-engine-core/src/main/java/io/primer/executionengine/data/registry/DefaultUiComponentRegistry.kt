package io.primer.executionengine.data.registry

import io.primer.executionengine.domain.registry.UiComponentRegistry
import java.util.concurrent.ConcurrentHashMap

internal class DefaultUiComponentRegistry : UiComponentRegistry {
    private val versions = ConcurrentHashMap<String, String>()

    override val declaredVersions: Map<String, String>
        get() = versions.toMap()

    override fun declare(type: String, version: String) {
        versions[type] = version
    }
}
