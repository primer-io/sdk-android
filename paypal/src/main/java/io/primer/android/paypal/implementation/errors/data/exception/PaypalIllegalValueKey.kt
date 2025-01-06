package io.primer.android.paypal.implementation.errors.data.exception

import io.primer.android.errors.data.exception.IllegalValueKey

internal enum class PaypalIllegalValueKey : IllegalValueKey {
    PAYMENT_METHOD_CONFIG_ID,
    INTENT_CHECKOUT_TOKEN,
    INTENT_VAULT_TOKEN,
    ;

    override val key = this.name
}
