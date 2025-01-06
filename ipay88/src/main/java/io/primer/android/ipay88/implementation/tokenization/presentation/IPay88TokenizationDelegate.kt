package io.primer.android.ipay88.implementation.tokenization.presentation

import io.primer.android.ipay88.implementation.configuration.domain.IPay88ConfigurationInteractor
import io.primer.android.ipay88.implementation.configuration.domain.model.IPay88ConfigParams
import io.primer.android.ipay88.implementation.tokenization.domain.IPay88TokenizationInteractor
import io.primer.android.ipay88.implementation.tokenization.domain.model.IPay88PaymentInstrumentParams
import io.primer.android.ipay88.implementation.tokenization.presentation.model.IPay88TokenizationInputable
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.presentation.PaymentMethodTokenizationDelegate
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationCollectedDataMapper

internal class IPay88TokenizationDelegate(
    private val configurationInteractor: IPay88ConfigurationInteractor,
    private val tokenizationInteractor: IPay88TokenizationInteractor,
) : PaymentMethodTokenizationDelegate<IPay88TokenizationInputable, IPay88PaymentInstrumentParams>(
        tokenizationInteractor,
    ),
    TokenizationCollectedDataMapper<IPay88TokenizationInputable, IPay88PaymentInstrumentParams> {
    override suspend fun mapTokenizationData(input: IPay88TokenizationInputable) =
        configurationInteractor(IPay88ConfigParams(paymentMethodType = input.paymentMethodType))
            .map { configuration ->
                TokenizationParams(
                    paymentInstrumentParams =
                        IPay88PaymentInstrumentParams(
                            paymentMethodType = input.paymentMethodType,
                            paymentMethodConfigId = configuration.paymentMethodConfigId,
                            locale = configuration.locale,
                        ),
                    sessionIntent = input.primerSessionIntent,
                )
            }
}
