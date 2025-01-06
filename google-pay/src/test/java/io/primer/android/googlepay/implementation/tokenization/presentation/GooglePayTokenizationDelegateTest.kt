package io.primer.android.googlepay.implementation.tokenization.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.googlepay.GooglePayFacade
import io.primer.android.googlepay.implementation.configuration.domain.GooglePayConfigurationInteractor
import io.primer.android.googlepay.implementation.configuration.domain.model.GooglePayConfiguration
import io.primer.android.googlepay.implementation.tokenization.domain.GooglePayTokenizationInteractor
import io.primer.android.googlepay.implementation.tokenization.domain.model.GooglePayFlow
import io.primer.android.googlepay.implementation.tokenization.presentation.composable.GooglePayTokenizationInputable
import io.primer.android.paymentmethods.core.configuration.domain.model.NoOpPaymentMethodConfigurationParams
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GooglePayTokenizationDelegateTest {
    private lateinit var configurationInteractor: GooglePayConfigurationInteractor
    private lateinit var tokenizationInteractor: GooglePayTokenizationInteractor
    private lateinit var delegate: GooglePayTokenizationDelegate
    private val input =
        GooglePayTokenizationInputable(
            paymentMethodType = "CARD",
            paymentData = mockk(),
            primerSessionIntent = PrimerSessionIntent.CHECKOUT,
        )

    @BeforeEach
    fun setUp() {
        configurationInteractor = mockk()
        tokenizationInteractor = mockk()
        delegate = GooglePayTokenizationDelegate(configurationInteractor, tokenizationInteractor)
    }

    @Test
    fun `mapTokenizationData returns success result when the configurationInteractor returns Result success`() =
        runTest {
            val configuration =
                GooglePayConfiguration(
                    environment = GooglePayFacade.Environment.TEST,
                    gatewayMerchantId = "gatewayMerchantId",
                    merchantName = "merchantName",
                    totalPrice = "totalPrice",
                    countryCode = "countryCode",
                    currencyCode = "currencyCode",
                    allowedCardNetworks = listOf("VISA", "MASTERCARD"),
                    allowedCardAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS"),
                    billingAddressRequired = true,
                    existingPaymentMethodRequired = false,
                    shippingOptions = null,
                    shippingAddressParameters = null,
                    requireShippingMethod = false,
                    emailAddressRequired = false,
                )
            coEvery { configurationInteractor(NoOpPaymentMethodConfigurationParams) } returns
                Result.success(configuration)

            val result = delegate.mapTokenizationData(input)

            assertTrue(result.isSuccess)
            val tokenizationParams = result.getOrNull()!!
            val paymentInstrumentParams = tokenizationParams.paymentInstrumentParams

            assertEquals(input.paymentMethodType, paymentInstrumentParams.paymentMethodType)
            assertEquals(configuration.gatewayMerchantId, paymentInstrumentParams.merchantId)
            assertEquals(input.paymentData, paymentInstrumentParams.paymentData)
            assertEquals(GooglePayFlow.GATEWAY, paymentInstrumentParams.flow)

            coVerify { configurationInteractor(NoOpPaymentMethodConfigurationParams) }
        }

    @Test
    fun `mapTokenizationData returns failure result when the configurationInteractor returns Result failure`() =
        runTest {
            val exception = Exception("Configuration error")
            coEvery { configurationInteractor(NoOpPaymentMethodConfigurationParams) } returns Result.failure(exception)

            val result = delegate.mapTokenizationData(input)

            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())

            coVerify { configurationInteractor(NoOpPaymentMethodConfigurationParams) }
        }
}
