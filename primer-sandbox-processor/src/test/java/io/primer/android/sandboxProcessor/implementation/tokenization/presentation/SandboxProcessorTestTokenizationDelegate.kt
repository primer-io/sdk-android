package io.primer.android.sandboxProcessor.implementation.tokenization.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.sandboxProcessor.SandboxProcessorDecisionType
import io.primer.android.sandboxProcessor.implementation.configuration.domain.ProcessorTestConfigurationInteractor
import io.primer.android.sandboxProcessor.implementation.configuration.domain.model.SandboxProcessorConfig
import io.primer.android.sandboxProcessor.implementation.configuration.domain.model.SandboxProcessorConfigParams
import io.primer.android.sandboxProcessor.implementation.tokenization.domain.ProcessorTestTokenizationInteractor
import io.primer.android.sandboxProcessor.implementation.tokenization.domain.model.SandboxProcessorPaymentInstrumentParams
import io.primer.android.sandboxProcessor.implementation.tokenization.presentation.composable.SandboxProcessorTokenizationInputable
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SandboxProcessorTestTokenizationDelegate {
    private val configurationInteractor = mockk<ProcessorTestConfigurationInteractor>()
    private val tokenizationInteractor = mockk<ProcessorTestTokenizationInteractor>()
    private val delegate = SandboxProcessorTokenizationDelegate(configurationInteractor, tokenizationInteractor)

    @Test
    fun `mapTokenizationData should return tokenization params successfully`() =
        runBlocking {
            val input =
                SandboxProcessorTokenizationInputable(
                    paymentMethodType = "PRIMER_TEST_PAYPAL",
                    primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                    decisionType = SandboxProcessorDecisionType.SUCCESS,
                )

            val configParams = SandboxProcessorConfigParams(paymentMethodType = input.paymentMethodType)
            val config = SandboxProcessorConfig(paymentMethodConfigId = "configId", locale = "en-US")

            coEvery { configurationInteractor.invoke(configParams) } returns Result.success(config)

            val result = delegate.mapTokenizationData(input)

            val expected =
                TokenizationParams(
                    paymentInstrumentParams =
                        SandboxProcessorPaymentInstrumentParams(
                            paymentMethodType = input.paymentMethodType,
                            paymentMethodConfigId = config.paymentMethodConfigId,
                            locale = config.locale,
                            flowDecision = SandboxProcessorDecisionType.SUCCESS.name,
                        ),
                    sessionIntent = input.primerSessionIntent,
                )

            assertEquals(Result.success(expected), result)
            coVerify { configurationInteractor.invoke(configParams) }
        }

    @Test
    fun `mapTokenizationData should return failure when configuration interactor fails`() =
        runBlocking {
            val input =
                SandboxProcessorTokenizationInputable(
                    paymentMethodType = "PRIMER_TEST_PAYPAL",
                    primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                    decisionType = SandboxProcessorDecisionType.SUCCESS,
                )

            val configParams = SandboxProcessorConfigParams(paymentMethodType = input.paymentMethodType)
            val error = Exception("Configuration error")

            coEvery { configurationInteractor.invoke(configParams) } returns Result.failure(error)

            val result = delegate.mapTokenizationData(input)

            assertEquals(Result.failure<SandboxProcessorPaymentInstrumentParams>(error), result)
            coVerify { configurationInteractor.invoke(configParams) }
        }
}
