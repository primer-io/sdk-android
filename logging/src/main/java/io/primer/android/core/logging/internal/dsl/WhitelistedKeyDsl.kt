package io.primer.android.core.logging.internal.dsl

import io.primer.android.core.logging.internal.WhitelistedKey

@DslMarker
internal annotation class WhitelistedKeyDsl

@WhitelistedKeyDsl
class WhitelistedKeyBuilder {
    private val keys = mutableListOf<WhitelistedKey>()

    fun primitiveKey(value: String) {
        keys += WhitelistedKey.PrimitiveWhitelistedKey(value = value)
    }

    fun nonPrimitiveKey(value: String, block: WhitelistedKeyBuilder.() -> Unit) {
        val childWhitelistedKeyBuilder = WhitelistedKeyBuilder().apply(block)
        keys += WhitelistedKey.NonPrimitiveWhitelistedKey(value, childWhitelistedKeyBuilder.build())
    }

    fun build(): List<WhitelistedKey> = keys
}

fun whitelistedKeys(block: WhitelistedKeyBuilder.() -> Unit): List<WhitelistedKey> =
    WhitelistedKeyBuilder().apply(block).build()
