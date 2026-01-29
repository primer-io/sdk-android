package io.primer.android.internal.domain.error

import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.error.models.PrimerError

/**
 * Exception wrapper for [PrimerError] to preserve error details through coroutine flows.
 *
 * Use this when you need to propagate a [PrimerError] through a [Result] or suspend function
 * that expects a [Throwable].
 *
 * @property primerError The underlying Primer error with full details
 * @property checkoutData Optional checkout data available even on failure
 */
internal class PrimerErrorException(
    val primerError: PrimerError,
    val checkoutData: PrimerCheckoutData? = null,
) : Exception(primerError.description)
