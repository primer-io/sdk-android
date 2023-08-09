package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.exception

import io.primer.android.data.base.exceptions.IllegalValueKey

internal enum class PaypalInvalidValueKey(override val key: String) : IllegalValueKey {

    ILLEGAL_AMOUNT("amount"),
    ILLEGAL_CURRENCY_CODE("currencyCode");
}
