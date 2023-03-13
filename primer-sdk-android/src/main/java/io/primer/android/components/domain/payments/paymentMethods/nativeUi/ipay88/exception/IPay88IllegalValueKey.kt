package io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.exception

import io.primer.android.data.base.exceptions.IllegalValueKey

internal enum class IPay88IllegalValueKey(override val key: String) : IllegalValueKey {

    ILLEGAL_AMOUNT("amount"),
    ILLEGAL_CURRENCY_CODE("currencyCode"),
    ILLEGAL_COUNTRY_CODE("order.countryCode"),
    ILLEGAL_PRODUCT_DESCRIPTION("order.lineItems.description"),
    ILLEGAL_CUSTOMER_FIRST_NAME("customer.firstName"),
    ILLEGAL_CUSTOMER_LAST_NAME("customer.lastName"),
    ILLEGAL_CUSTOMER_EMAIL("customer.emailAddress"),
    ILLEGAL_CUSTOMER_ID("customer.id");
}
