package io.primer.android.vouchers.retailOutlets.implementation.tokenization.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.PrimerRetailerData
import io.primer.android.PrimerSessionIntent
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.vouchers.retailOutlets.implementation.configuration.domain.RetailOutletsConfigurationInteractor
import io.primer.android.vouchers.retailOutlets.implementation.configuration.domain.model.RetailOutletsConfig
import io.primer.android.vouchers.retailOutlets.implementation.configuration.domain.model.RetailOutletsConfigParams
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.domain.RetailOutletsTokenizationInteractor
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.domain.model.RetailOutletsPaymentInstrumentParams
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.presentation.composable.RetailOutletsTokenizationInputable
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RetailOutletsTokenizationDelegateTest {
    private val configurationInteractor = mockk<RetailOutletsConfigurationInteractor>()
    private val tokenizationInteractor = mockk<RetailOutletsTokenizationInteractor>()
    private val delegate = RetailOutletsTokenizationDelegate(configurationInteractor, tokenizationInteractor)

    @Test
    fun `mapTokenizationData should return tokenization params successfully`() =
        runBlocking {
            val retailerData = mockk<PrimerRetailerData>(relaxed = true)
            val input =
                RetailOutletsTokenizationInputable(
                    paymentMethodType = "multibanco",
                    primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                    retailOutletData = retailerData,
                )

            val configParams = RetailOutletsConfigParams(paymentMethodType = input.paymentMethodType)
            val config = RetailOutletsConfig(paymentMethodConfigId = "configId", locale = "en-US")

            coEvery { configurationInteractor.invoke(configParams) } returns Result.success(config)

            val result = delegate.mapTokenizationData(input)

            val expected =
                TokenizationParams(
                    paymentInstrumentParams =
                    RetailOutletsPaymentInstrumentParams(
                        paymentMethodType = input.paymentMethodType,
                        paymentMethodConfigId = config.paymentMethodConfigId,
                        locale = config.locale,
                        retailOutlet = retailerData.id,
                    ),
                    sessionIntent = input.primerSessionIntent,
                )

            assertEquals(Result.success(expected), result)
            coVerify { configurationInteractor.invoke(configParams) }
        }

    @Test
    fun `mapTokenizationData should return failure when configuration interactor fails`() =
        runBlocking {
            val retailerData = mockk<PrimerRetailerData>(relaxed = true)
            val input =
                RetailOutletsTokenizationInputable(
                    paymentMethodType = "multibanco",
                    primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                    retailOutletData = retailerData,
                )

            val configParams = RetailOutletsConfigParams(paymentMethodType = input.paymentMethodType)
            val error = Exception("Configuration error")

            coEvery { configurationInteractor.invoke(configParams) } returns Result.failure(error)

            val result = delegate.mapTokenizationData(input)

            assertEquals(Result.failure<RetailOutletsPaymentInstrumentParams>(error), result)
            coVerify { configurationInteractor.invoke(configParams) }
        }
}
