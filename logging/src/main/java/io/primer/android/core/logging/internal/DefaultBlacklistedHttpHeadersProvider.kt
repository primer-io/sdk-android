package io.primer.android.core.logging.internal

import io.primer.android.core.logging.BlacklistedHttpHeadersProvider

class DefaultBlacklistedHttpHeadersProvider(
    override val values: List<String> = listOf("Primer-Client-Token")
) : BlacklistedHttpHeadersProvider
