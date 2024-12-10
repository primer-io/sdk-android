package io.primer.android.klarna.implementation.session.data.exception

import io.primer.android.errors.data.exception.IllegalValueKey

internal enum class KlarnaIllegalValueKey(override val key: String) :
    IllegalValueKey {
    PAYMENT_METHOD_CONFIG_ID("configuration.id"),
    ORDER_LINE_ITEM_DESCRIPTION("order.line.item.description"),
    ORDER_LINE_ITEM_UNIT_AMOUNT("order.line.item.unit.amount"),
    TOTAL_ORDER_AMOUNT("total.order.amount")
}
