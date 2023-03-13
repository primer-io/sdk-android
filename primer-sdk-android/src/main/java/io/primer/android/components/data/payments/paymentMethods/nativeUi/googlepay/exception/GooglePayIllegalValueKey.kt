package io.primer.android.components.data.payments.paymentMethods.nativeUi.googlepay.exception

import io.primer.android.data.base.exceptions.IllegalValueKey

internal enum class GooglePayIllegalValueKey(override val key: String) : IllegalValueKey {
    MERCHANT_ID("configuration.merchantId"),
    SDK_PAYMENT_DATA("google_pay.sdk_payment_data");
}
