package io.primer.android.paypal.implementation.tokenization.presentation.model

import io.primer.android.PrimerSessionIntent
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationInputable

internal sealed class PaypalTokenizationInputable(
    override val paymentMethodType: String,
    override val primerSessionIntent: PrimerSessionIntent
) : TokenizationInputable {

    data class PaypalCheckoutTokenizationInputable(
        val orderId: String?,
        val paymentMethodConfigId: String,
        override val paymentMethodType: String,
        override val primerSessionIntent: PrimerSessionIntent
    ) : PaypalTokenizationInputable(
        paymentMethodType = paymentMethodType,
        primerSessionIntent = primerSessionIntent
    )

    data class PaypalVaultTokenizationInputable(
        val tokenId: String?,
        val paymentMethodConfigId: String,
        override val paymentMethodType: String,
        override val primerSessionIntent: PrimerSessionIntent
    ) : PaypalTokenizationInputable(
        paymentMethodType = paymentMethodType,
        primerSessionIntent = primerSessionIntent
    )
}
