package io.primer.android.paypal.implementation.errors.domain.exception

import io.primer.android.errors.data.exception.IllegalValueKey

internal enum class PaypalInvalidValueKey(override val key: String) :
    IllegalValueKey {
    ILLEGAL_AMOUNT("amount"),
    ILLEGAL_CURRENCY_CODE("currencyCode"),
}
