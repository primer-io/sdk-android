package io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.exception

import io.primer.android.data.base.exceptions.IllegalValueKey

internal enum class ApayaIllegalValueKey(override val key: String) : IllegalValueKey {
    MERCHANT_ID("configuration.merchantId"),
    MERCHANT_ACCOUNT_ID("configuration.merchantAccountId"),
    CUSTOMER_MOBILE_NUMBER("customer.mobileNumber");
}
