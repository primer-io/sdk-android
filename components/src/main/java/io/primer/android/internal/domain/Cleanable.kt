package io.primer.android.internal.domain

/**
 * Interface for payment flow components that need cleanup when dismissed.
 * Implementations should cancel in-flight requests but remain reusable.
 */
internal fun interface Cleanable {
    fun cleanup()
}
