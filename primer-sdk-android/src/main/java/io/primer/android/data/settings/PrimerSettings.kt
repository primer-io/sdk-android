package io.primer.android.data.settings

import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.data.configuration.models.CustomerDataResponse
import io.primer.android.data.configuration.models.OrderDataResponse
import io.primer.android.ui.settings.PrimerUIOptions
import java.util.Locale

data class PrimerSettings(
    var paymentHandling: PrimerPaymentHandling = PrimerPaymentHandling.AUTO,
    var locale: Locale = Locale.getDefault(),
    var paymentMethodOptions: PrimerPaymentMethodOptions = PrimerPaymentMethodOptions(),
    var uiOptions: PrimerUIOptions = PrimerUIOptions(),
    var debugOptions: PrimerDebugOptions = PrimerDebugOptions(),
) {

    internal var fromHUC: Boolean = false

    internal var order = OrderDataResponse()
    internal var customer = CustomerDataResponse()

    internal val sdkIntegrationType: SdkIntegrationType
        get() = when (fromHUC) {
            true -> SdkIntegrationType.HEADLESS
            false -> SdkIntegrationType.DROP_IN
        }

    val currency: String
        @Throws
        get() = order.currencyCode ?: throw IllegalArgumentException(CURRENCY_EXCEPTION)

    val currentAmount: Int
        @Throws
        get() = order.merchantAmount ?: order.totalOrderAmount ?: throw IllegalArgumentException(
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
