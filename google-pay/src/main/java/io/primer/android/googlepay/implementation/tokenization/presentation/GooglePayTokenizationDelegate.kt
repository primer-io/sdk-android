package io.primer.android.googlepay.implementation.tokenization.presentation

import io.primer.android.googlepay.implementation.configuration.domain.GooglePayConfigurationInteractor
import io.primer.android.googlepay.implementation.tokenization.domain.GooglePayTokenizationInteractor
import io.primer.android.googlepay.implementation.tokenization.domain.model.GooglePayFlow
import io.primer.android.googlepay.implementation.tokenization.domain.model.GooglePayPaymentInstrumentParams
import io.primer.android.googlepay.implementation.tokenization.presentation.composable.GooglePayTokenizationInputable
import io.primer.android.paymentmethods.core.configuration.domain.model.NoOpPaymentMethodConfigurationParams
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.presentation.PaymentMethodTokenizationDelegate
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationCollectedDataMapper

internal class GooglePayTokenizationDelegate(
    private val configurationInteractor: GooglePayConfigurationInteractor,
    tokenizationInteractor: GooglePayTokenizationInteractor
) : PaymentMethodTokenizationDelegate<GooglePayTokenizationInputable, GooglePayPaymentInstrumentParams>(
    tokenizationInteractor
),
    TokenizationCollectedDataMapper<GooglePayTokenizationInputable, GooglePayPaymentInstrumentParams> {

    override suspend fun mapTokenizationData(input: GooglePayTokenizationInputable):
        Result<TokenizationParams<GooglePayPaymentInstrumentParams>> = configurationInteractor.invoke(
        NoOpPaymentMethodConfigurationParams
    ).map { configuration ->
        TokenizationParams(
            GooglePayPaymentInstrumentParams(
                input.paymentMethodType,
                configuration.gatewayMerchantId,
                input.paymentData,
                GooglePayFlow.GATEWAY
            ),
            input.primerSessionIntent
        )
    }
}
