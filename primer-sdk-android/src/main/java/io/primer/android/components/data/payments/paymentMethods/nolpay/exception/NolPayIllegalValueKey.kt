package io.primer.android.components.data.payments.paymentMethods.nolpay.exception

import io.primer.android.data.base.exceptions.IllegalValueKey

internal enum class NolPayIllegalValueKey(override val key: String) : IllegalValueKey {
    MERCHANT_APP_ID("configuration.merchantAppId");
}
