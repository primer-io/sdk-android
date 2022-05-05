package io.primer.android.model.dto

import io.primer.android.data.configuration.model.CustomerDataResponse
import io.primer.android.data.configuration.model.OrderDataResponse
import kotlinx.serialization.Serializable

@Serializable
data class PrimerSettings(
    var business: Business = Business(),
    var options: Options = Options(),
) {

    internal var order = OrderDataResponse()
    internal var customer = CustomerDataResponse()

    val currency: String
        @Throws
        get() = order.currency ?: throw IllegalArgumentException(CURRENCY_EXCEPTION)

    val currentAmount: Int
        @Throws
        get() = order.amount ?: order.totalOrderAmount ?: throw IllegalArgumentException(
            AMOUNT_EXCEPTION
        )

    companion object {
        private const val EXCEPTION_MESSAGE = "required but not found. Please set this value"
        private const val DOCS_REFERENCE = """when generating the client session with 
POST /client-session. See documentation here: https://primer.io/docs/api#tag/Client-Session"""

        const val CURRENCY_EXCEPTION = "Currency $EXCEPTION_MESSAGE $DOCS_REFERENCE"
        const val AMOUNT_EXCEPTION = "Amount $EXCEPTION_MESSAGE $DOCS_REFERENCE"
    }
}
