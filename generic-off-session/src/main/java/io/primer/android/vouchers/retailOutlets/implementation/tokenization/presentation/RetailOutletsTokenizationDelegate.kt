package io.primer.android.vouchers.retailOutlets.implementation.tokenization.presentation

import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.presentation.PaymentMethodTokenizationDelegate
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationCollectedDataMapper
import io.primer.android.vouchers.retailOutlets.implementation.configuration.domain.RetailOutletsConfigurationInteractor
import io.primer.android.vouchers.retailOutlets.implementation.configuration.domain.model.RetailOutletsConfigParams
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.domain.RetailOutletsTokenizationInteractor
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.domain.model.RetailOutletsPaymentInstrumentParams
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.presentation.composable.RetailOutletsTokenizationInputable

internal class RetailOutletsTokenizationDelegate(
    private val configurationInteractor: RetailOutletsConfigurationInteractor,
    tokenizationInteractor: RetailOutletsTokenizationInteractor,
) : PaymentMethodTokenizationDelegate<RetailOutletsTokenizationInputable, RetailOutletsPaymentInstrumentParams>(
    tokenizationInteractor,
),
    TokenizationCollectedDataMapper<
        RetailOutletsTokenizationInputable,
        RetailOutletsPaymentInstrumentParams,
        > {
    override suspend fun mapTokenizationData(
        input: RetailOutletsTokenizationInputable,
    ): Result<TokenizationParams<RetailOutletsPaymentInstrumentParams>> =
        configurationInteractor(
            RetailOutletsConfigParams(paymentMethodType = input.paymentMethodType),
        ).map { configuration ->
            TokenizationParams(
                paymentInstrumentParams =
                RetailOutletsPaymentInstrumentParams(
                    paymentMethodType = input.paymentMethodType,
                    paymentMethodConfigId = configuration.paymentMethodConfigId,
                    locale = configuration.locale,
                    retailOutlet = input.retailOutletData.id,
                ),
                sessionIntent = input.primerSessionIntent,
            )
        }
}
