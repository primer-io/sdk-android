package io.primer.android.vouchers.multibanco.implementation.tokenization.presentation

import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.presentation.PaymentMethodTokenizationDelegate
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationCollectedDataMapper
import io.primer.android.vouchers.multibanco.implementation.configuration.domain.MultibancoConfigurationInteractor
import io.primer.android.vouchers.multibanco.implementation.configuration.domain.model.MultibancoConfigParams
import io.primer.android.vouchers.multibanco.implementation.tokenization.domain.MultibancoTokenizationInteractor
import io.primer.android.vouchers.multibanco.implementation.tokenization.domain.model.MultibancoPaymentInstrumentParams
import io.primer.android.vouchers.multibanco.implementation.tokenization.presentation.composable.MultibancoTokenizationInputable

internal class MultibancoTokenizationDelegate(
    private val configurationInteractor: MultibancoConfigurationInteractor,
    tokenizationInteractor: MultibancoTokenizationInteractor,
) : PaymentMethodTokenizationDelegate<MultibancoTokenizationInputable, MultibancoPaymentInstrumentParams>(
        tokenizationInteractor,
    ),
    TokenizationCollectedDataMapper<
        MultibancoTokenizationInputable,
        MultibancoPaymentInstrumentParams,
        > {
    override suspend fun mapTokenizationData(
        input: MultibancoTokenizationInputable,
    ): Result<TokenizationParams<MultibancoPaymentInstrumentParams>> =
        configurationInteractor(
            MultibancoConfigParams(paymentMethodType = input.paymentMethodType),
        ).map { configuration ->
            TokenizationParams(
                paymentInstrumentParams =
                    MultibancoPaymentInstrumentParams(
                        paymentMethodType = input.paymentMethodType,
                        paymentMethodConfigId = configuration.paymentMethodConfigId,
                        locale = configuration.locale,
                    ),
                sessionIntent = input.primerSessionIntent,
            )
        }
}
