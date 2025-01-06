package io.primer.android.sandboxProcessor.implementation.tokenization.presentation

import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.presentation.PaymentMethodTokenizationDelegate
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationCollectedDataMapper
import io.primer.android.sandboxProcessor.implementation.configuration.domain.ProcessorTestConfigurationInteractor
import io.primer.android.sandboxProcessor.implementation.configuration.domain.model.SandboxProcessorConfigParams
import io.primer.android.sandboxProcessor.implementation.tokenization.domain.ProcessorTestTokenizationInteractor
import io.primer.android.sandboxProcessor.implementation.tokenization.domain.model.SandboxProcessorPaymentInstrumentParams
import io.primer.android.sandboxProcessor.implementation.tokenization.presentation.composable.SandboxProcessorTokenizationInputable

internal class SandboxProcessorTokenizationDelegate(
    private val configurationInteractor: ProcessorTestConfigurationInteractor,
    tokenizationInteractor: ProcessorTestTokenizationInteractor,
) : PaymentMethodTokenizationDelegate<SandboxProcessorTokenizationInputable, SandboxProcessorPaymentInstrumentParams>(
        tokenizationInteractor,
    ),
    TokenizationCollectedDataMapper<SandboxProcessorTokenizationInputable, SandboxProcessorPaymentInstrumentParams> {
    override suspend fun mapTokenizationData(
        input: SandboxProcessorTokenizationInputable,
    ): Result<TokenizationParams<SandboxProcessorPaymentInstrumentParams>> =
        configurationInteractor(
            SandboxProcessorConfigParams(paymentMethodType = input.paymentMethodType),
        ).map { configuration ->
            TokenizationParams(
                paymentInstrumentParams =
                    SandboxProcessorPaymentInstrumentParams(
                        paymentMethodType = input.paymentMethodType,
                        paymentMethodConfigId = configuration.paymentMethodConfigId,
                        locale = configuration.locale,
                        flowDecision = input.decisionType.name,
                    ),
                sessionIntent = input.primerSessionIntent,
            )
        }
}
