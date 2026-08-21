package io.primer.executionengine.domain.handler

/**
 * Why the browser presenting a `url.open` step was closed: [AUTO] when the redirect flow
 * completed and closed it, [USER] when the user dismissed it.
 */
enum class UrlCloseReason(val value: String) {
    AUTO("auto"),
    USER("user"),
}
