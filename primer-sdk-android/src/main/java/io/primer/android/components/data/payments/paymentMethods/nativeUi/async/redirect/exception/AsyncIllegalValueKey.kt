package io.primer.android.components.data.payments.paymentMethods.nativeUi.async.redirect.exception

import io.primer.android.data.base.exceptions.IllegalValueKey

internal enum class AsyncIllegalValueKey(override val key: String) : IllegalValueKey {
    PAYMENT_METHOD_CONFIG_ID("configuration.id");
}
