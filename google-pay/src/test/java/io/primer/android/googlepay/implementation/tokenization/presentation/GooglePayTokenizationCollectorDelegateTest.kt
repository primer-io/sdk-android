package io.primer.android.googlepay.implementation.tokenization.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.data.settings.PrimerGoogleShippingAddressParameters
import io.primer.android.configuration.data.model.ShippingMethod
import io.primer.android.configuration.domain.model.CheckoutModule
import io.primer.android.googlepay.GooglePayFacade
import io.primer.android.googlepay.implementation.composer.ui.navigation.launcher.GooglePayActivityLauncherParams
import io.primer.android.googlepay.implementation.configuration.domain.GooglePayConfigurationInteractor
import io.primer.android.googlepay.implementation.configuration.domain.model.GooglePayConfiguration
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.paymentmethods.core.configuration.domain.model.NoOpPaymentMethodConfigurationParams
import io.primer.android.payments.core.tokenization.presentation.composable.NoOpPaymentMethodTokenizationCollectorParams
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GooglePayTokenizationCollectorDelegateTest {

    private lateinit var configurationInteractor: GooglePayConfigurationInteractor
    private lateinit var delegate: GooglePayTokenizationCollectorDelegate

    @BeforeEach
    fun setUp() {
        configurationInteractor = mockk()
        delegate = GooglePayTokenizationCollectorDelegate(configurationInteractor)
    }

    @Test
    fun `startDataCollection() should create a GooglePayActivityLauncherParams when the configurationInteractor returns Result success`() = runTest {
        val configuration = GooglePayConfiguration(
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
            shippingOptions = CheckoutModule.Shipping(
                selectedMethod = "STANDARD_SHIPPING",
                shippingMethods = listOf(
                    ShippingMethod("STANDARD_SHIPPING", "Standard Shipping", 1, "1")
                )
            ),
            shippingAddressParameters = PrimerGoogleShippingAddressParameters(phoneNumberRequired = true),
            requireShippingMethod = true,
            emailAddressRequired = true
        )
        coEvery { configurationInteractor(NoOpPaymentMethodConfigurationParams) } returns Result.success(configuration)

        launch {
            delegate.startDataCollection(NoOpPaymentMethodTokenizationCollectorParams)
        }

        val uiEvent = delegate.uiEvent.first()
        assertTrue(uiEvent is ComposerUiEvent.Navigate)

        val navigateEvent = uiEvent as ComposerUiEvent.Navigate
        with(navigateEvent.params as GooglePayActivityLauncherParams) {
            assertEquals(configuration.environment, environment)
            assertEquals(configuration.gatewayMerchantId, gatewayMerchantId)
            assertEquals(configuration.merchantName, merchantName)
            assertEquals(configuration.totalPrice, totalPrice)
            assertEquals(configuration.countryCode, countryCode)
            assertEquals(configuration.currencyCode, currencyCode)
            assertEquals(configuration.allowedCardNetworks, allowedCardNetworks)
            assertEquals(configuration.allowedCardAuthMethods, allowedCardAuthMethods)
            assertEquals(configuration.billingAddressRequired, billingAddressRequired)
            assertEquals(configuration.shippingOptions, shippingOptions)
            assertEquals(configuration.shippingAddressParameters, shippingAddressParameters)
            assertEquals(configuration.requireShippingMethod, requireShippingMethod)
            assertEquals(configuration.emailAddressRequired, emailAddressRequired)
        }

        coVerify { configurationInteractor(NoOpPaymentMethodConfigurationParams) }
    }
}
