package io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.exception

import io.primer.android.data.base.exceptions.IllegalValueKey

internal enum class PaypalIllegalValueKey : IllegalValueKey {
    PAYMENT_METHOD_CONFIG_ID,
    INTENT_CHECKOUT_TOKEN,
    INTENT_VAULT_TOKEN;

    override val key = this.name
}
