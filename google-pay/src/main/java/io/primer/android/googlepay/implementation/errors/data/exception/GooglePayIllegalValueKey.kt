package io.primer.android.googlepay.implementation.errors.data.exception

import io.primer.android.errors.data.exception.IllegalValueKey

internal enum class GooglePayIllegalValueKey(override val key: String) : IllegalValueKey {
    MERCHANT_ID("configuration.merchantId"),
    SDK_PAYMENT_DATA("google_pay.sdk_payment_data"),
}
