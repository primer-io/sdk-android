package io.primer.android.klarna.implementation.errors.data.exception

import io.primer.android.errors.data.exception.IllegalValueKey

internal enum class KlarnaIllegalValueKey(override val key: String) :
    IllegalValueKey {
    KLARNA_SESSION("klarnaSession"),
    KLARNA_PAYMENT_VIEW("klarnaPaymentView"),
}
