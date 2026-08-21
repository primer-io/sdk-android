package io.primer.executionengine.data.models.http

internal enum class HttpMethod {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
    ;

    companion object {
        fun safeValueOf(value: String?): HttpMethod? = entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}
