package io.primer.android.core.logging.internal

internal sealed interface WhitelistedKey {
    val value: String

    data class PrimitiveWhitelistedKey(
        override val value: String
    ) : WhitelistedKey

    data class NonPrimitiveWhitelistedKey(
        override val value: String,
        val children: List<WhitelistedKey>
    ) : WhitelistedKey
}
