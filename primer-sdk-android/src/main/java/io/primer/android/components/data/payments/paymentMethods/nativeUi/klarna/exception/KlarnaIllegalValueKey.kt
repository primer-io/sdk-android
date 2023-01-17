package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.exception

import io.primer.android.data.base.exceptions.IllegalValueKey

internal enum class KlarnaIllegalValueKey : IllegalValueKey {
    PAYMENT_METHOD_CONFIG_ID;

    override val key = this.name
}
