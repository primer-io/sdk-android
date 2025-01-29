package io.primer.android.vouchers.multibanco.implementation.tokenization.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.vouchers.multibanco.implementation.configuration.domain.MultibancoConfigurationInteractor
import io.primer.android.vouchers.multibanco.implementation.configuration.domain.model.MultibancoConfig
import io.primer.android.vouchers.multibanco.implementation.configuration.domain.model.MultibancoConfigParams
import io.primer.android.vouchers.multibanco.implementation.tokenization.domain.MultibancoTokenizationInteractor
import io.primer.android.vouchers.multibanco.implementation.tokenization.domain.model.MultibancoPaymentInstrumentParams
import io.primer.android.vouchers.multibanco.implementation.tokenization.presentation.composable.MultibancoTokenizationInputable
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MultibancoTokenizationDelegateTest {
    private val configurationInteractor = mockk<MultibancoConfigurationInteractor>()
    private val tokenizationInteractor = mockk<MultibancoTokenizationInteractor>()
    private val delegate = MultibancoTokenizationDelegate(configurationInteractor, tokenizationInteractor)

    @Test
    fun `mapTokenizationData should return tokenization params successfully`() =
        runBlocking {
            val input =
                MultibancoTokenizationInputable(
                    paymentMethodType = "multibanco",
                    primerSessionIntent = mockk(),
                )

            val configParams = MultibancoConfigParams(paymentMethodType = input.paymentMethodType)
            val config = MultibancoConfig(paymentMethodConfigId = "configId", locale = "en-US")

            coEvery { configurationInteractor.invoke(configParams) } returns Result.success(config)

            val result = delegate.mapTokenizationData(input)

            val expected =
                TokenizationParams(
                    paymentInstrumentParams =
                    MultibancoPaymentInstrumentParams(
                        paymentMethodType = input.paymentMethodType,
                        paymentMethodConfigId = config.paymentMethodConfigId,
                        locale = config.locale,
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
                MultibancoTokenizationInputable(
                    paymentMethodType = "multibanco",
                    primerSessionIntent = mockk(),
                )

            val configParams = MultibancoConfigParams(paymentMethodType = input.paymentMethodType)
            val error = Exception("Configuration error")

            coEvery { configurationInteractor.invoke(configParams) } returns Result.failure(error)

            val result = delegate.mapTokenizationData(input)

            assertEquals(Result.failure<MultibancoPaymentInstrumentParams>(error), result)
            coVerify { configurationInteractor.invoke(configParams) }
        }
}
