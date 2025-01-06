package io.primer.android.vouchers.multibanco.implementation.composer

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.extensions.flatMap
import io.primer.android.paymentmethods.core.composer.InternalNativeUiPaymentMethodComponent
import io.primer.android.vouchers.multibanco.implementation.payment.delegate.MultibancoPaymentDelegate
import io.primer.android.vouchers.multibanco.implementation.tokenization.presentation.MultibancoTokenizationDelegate
import io.primer.android.vouchers.multibanco.implementation.tokenization.presentation.composable.MultibancoTokenizationInputable
import kotlinx.coroutines.launch

internal class MultibancoComponent(
    private val tokenizationDelegate: MultibancoTokenizationDelegate,
    private val paymentDelegate: MultibancoPaymentDelegate,
) : InternalNativeUiPaymentMethodComponent() {
    override fun start(
        paymentMethodType: String,
        primerSessionIntent: PrimerSessionIntent,
    ) {
        this.paymentMethodType = paymentMethodType
        this.primerSessionIntent = primerSessionIntent

        startTokenization()
    }

    private fun startTokenization() =
        composerScope.launch {
            tokenizationDelegate.tokenize(
                MultibancoTokenizationInputable(
                    paymentMethodType = paymentMethodType,
                    primerSessionIntent = primerSessionIntent,
                ),
            ).flatMap { paymentMethodTokenData ->
                paymentDelegate.handlePaymentMethodToken(
                    paymentMethodTokenData = paymentMethodTokenData,
                    primerSessionIntent = primerSessionIntent,
                )
            }.onFailure {
                paymentDelegate.handleError(it)
            }
        }
}
