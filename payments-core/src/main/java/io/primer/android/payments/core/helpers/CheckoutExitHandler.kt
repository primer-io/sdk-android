package io.primer.android.payments.core.helpers

import kotlinx.coroutines.flow.Flow

/**
 * Dispatches checkout exit via callbacks. Exposes a flow that emits whenever checkout is exited.
 */
interface CheckoutExitHandler {
    /**
     * Flow that emits whenever checkout is exited.
     */
    val checkoutExited: Flow<Unit>

    /**
     * Dispatches checkout exit event.
     */
    fun handle()
}
