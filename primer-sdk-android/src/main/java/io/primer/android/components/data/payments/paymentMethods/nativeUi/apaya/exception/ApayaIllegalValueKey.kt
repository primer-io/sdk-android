package io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.exception

import io.primer.android.data.base.exceptions.IllegalValueKey

internal enum class ApayaIllegalValueKey : IllegalValueKey {
    MERCHANT_ID,
    MERCHANT_ACCOUNT_ID,
    CUSTOMER_MOBILE_NUMBER;

    override val key = this.name
}
