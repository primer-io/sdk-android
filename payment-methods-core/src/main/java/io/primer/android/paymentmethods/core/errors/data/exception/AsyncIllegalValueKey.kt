package io.primer.android.paymentmethods.core.errors.data.exception

import io.primer.android.errors.data.exception.IllegalValueKey

enum class AsyncIllegalValueKey(override val key: String) : IllegalValueKey {
    PAYMENT_METHOD_CONFIG_ID("configuration.id"),
}
