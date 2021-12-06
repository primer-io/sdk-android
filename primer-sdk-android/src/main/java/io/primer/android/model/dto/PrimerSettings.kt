package io.primer.android.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class PrimerSettings(
    @Deprecated(ORDER_DEPRECATION_MESSAGE) var order: Order = Order(),
    @Deprecated(CUSTOMER_DEPRECATION_MESSAGE) var customer: Customer = Customer(),
    var business: Business = Business(),
    var options: Options = Options(),
) {
    val currency: String
        @Throws
        get() = order.currency ?: throw Exception(CURRENCY_EXCEPTION)

    val currentAmount: Int
        @Throws
        get() = order.totalOrderAmount ?: order.amount ?: throw Exception(AMOUNT_EXCEPTION)

    companion object {
        private const val EXCEPTION_MESSAGE = "required but not found. Please set this value"
        private const val DOCS_REFERENCE = """when generating the client session with 
POST /client-session. See documentation here: https://primer.io/docs/api#tag/Client-Session"""

        const val CURRENCY_EXCEPTION = "Currency $EXCEPTION_MESSAGE $DOCS_REFERENCE"
        const val AMOUNT_EXCEPTION = "Amount $EXCEPTION_MESSAGE $DOCS_REFERENCE"
        const val ORDER_DEPRECATION_MESSAGE = "Please set order details $DOCS_REFERENCE"
        const val CUSTOMER_DEPRECATION_MESSAGE = "Please set customer details $DOCS_REFERENCE"
    }
}
