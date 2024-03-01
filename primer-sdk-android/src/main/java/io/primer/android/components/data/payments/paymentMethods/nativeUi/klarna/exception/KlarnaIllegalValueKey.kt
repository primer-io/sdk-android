package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.exception

import io.primer.android.data.base.exceptions.IllegalValueKey

internal enum class KlarnaIllegalValueKey(override val key: String) : IllegalValueKey {
    PAYMENT_METHOD_CONFIG_ID("configuration.id"),
    ORDER_LINE_ITEM_DESCRIPTION("order.line.item.description"),
    ORDER_LINE_ITEM_UNIT_AMOUNT("order.line.item.unit.amount"),
    TOTAL_ORDER_AMOUNT("total.order.amount")
}
