package io.primer.android.components.data.payments.paymentMethods.nativeUi.googlepay.exception

import io.primer.android.data.base.exceptions.IllegalValueKey

internal enum class GooglePayIllegalValueKey : IllegalValueKey {
    MERCHANT_ID,
    SDK_PAYMENT_DATA;

    override val key = this.name
}
