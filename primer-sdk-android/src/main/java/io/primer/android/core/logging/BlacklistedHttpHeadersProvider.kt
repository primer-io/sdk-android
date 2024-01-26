package io.primer.android.core.logging

internal interface BlacklistedHttpHeadersProvider {
    val values: List<String>
}
