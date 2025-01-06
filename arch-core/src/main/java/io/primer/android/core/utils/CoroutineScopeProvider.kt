package io.primer.android.core.utils

import kotlinx.coroutines.CoroutineScope

/**
 * A provider interface for supplying a [CoroutineScope].
 *
 * Implementers of this interface are responsible for providing
 * a valid [CoroutineScope] to be used for coroutine execution.
 *
 * ### Example: Parent-Child CoroutineScope relationship
 *
 * This interface can be used to create a parent-child relationship
 * between coroutine scopes. For instance, you can have a parent
 * component that provides its [CoroutineScope] to a child, allowing the
 * child scope to be automatically canceled when the parent is canceled.
 *
 */
interface CoroutineScopeProvider {
    /**
     * The [CoroutineScope] that should be used for launching coroutines.
     */
    val scope: CoroutineScope
}
