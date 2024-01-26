package io.primer.android.core.logging

import io.primer.android.core.logging.internal.WhitelistedKey

internal interface WhitelistedHttpBodyKeysProvider {
    val values: List<WhitelistedKey>
}
