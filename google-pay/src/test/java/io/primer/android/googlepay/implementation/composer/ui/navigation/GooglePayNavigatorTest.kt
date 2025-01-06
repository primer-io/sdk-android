package io.primer.android.googlepay.implementation.composer.ui.navigation

import android.app.Activity
import android.content.Intent
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.googlepay.GooglePayFacade
import io.primer.android.googlepay.GooglePayFacadeFactory
import io.primer.android.googlepay.implementation.composer.ui.navigation.launcher.GooglePayActivityLauncherParams
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GooglePayNavigatorTest {
    private lateinit var activity: Activity
    private lateinit var logReporter: LogReporter
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var facade: GooglePayFacade
    private lateinit var navigator: GooglePayNavigator

    @BeforeEach
    fun setUp() {
        activity =
            mockk(relaxed = true) {
                every { applicationContext } returns mockk(relaxed = true)
            }

        facade = mockk<GooglePayFacade>(relaxed = true)
        val facadeFactory =
            mockk<GooglePayFacadeFactory> {
                every { create(any(), any(), any()) } returns facade
            }

        logReporter = mockk(relaxed = true)
        launcher = mockk(relaxed = true)
        navigator = GooglePayNavigator(activity, logReporter, launcher, facadeFactory)
        mockkStatic(Looper::class)
    }

    @Test
    fun `navigate() should call GooglePayFacade's pay method with correct parameters`() {
        // Given
        val params =
            GooglePayActivityLauncherParams(
                environment = GooglePayFacade.Environment.TEST,
                gatewayMerchantId = "merchant_id",
                merchantName = "Merchant",
                totalPrice = "100.0",
                countryCode = "US",
                currencyCode = "USD",
                allowedCardNetworks = listOf("VISA", "MASTERCARD"),
                allowedCardAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS"),
                billingAddressRequired = true,
                shippingOptions = null,
                shippingAddressParameters = null,
                requireShippingMethod = false,
                emailAddressRequired = false,
            )
        every { Looper.getMainLooper() } returns mockk(relaxed = true)

        // When
        navigator.navigate(params)

        // Then
        verify {
            facade.pay(
                activity = activity,
                gatewayMerchantId = "merchant_id",
                merchantName = "Merchant",
                totalPrice = "100.0",
                countryCode = "US",
                currencyCode = "USD",
                allowedCardNetworks = listOf("VISA", "MASTERCARD"),
                allowedCardAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS"),
                billingAddressRequired = true,
                shippingOptions = null,
                shippingAddressParameters = null,
                requireShippingMethod = false,
                emailAddressRequired = false,
            )
        }
    }

    @Test
    fun `canHandle() should return true when called with GooglePayActivityLauncherParams`() {
        // Given
        val params =
            GooglePayActivityLauncherParams(
                environment = GooglePayFacade.Environment.TEST,
                gatewayMerchantId = "merchant_id",
                merchantName = "Merchant",
                totalPrice = "100.0",
                countryCode = "US",
                currencyCode = "USD",
                allowedCardNetworks = listOf("VISA", "MASTERCARD"),
                allowedCardAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS"),
                billingAddressRequired = true,
                shippingOptions = null,
                shippingAddressParameters = null,
                requireShippingMethod = false,
                emailAddressRequired = false,
            )

        // When
        val result = navigator.canHandle(params)

        // Then
        assert(result)
    }

    @Test
    fun `canHandle() should return false when called with non-GooglePayActivityLauncherParams`() {
        // Given
        val params = mockk<NavigationParams>()

        // When
        val result = navigator.canHandle(params)

        // Then
        assert(!result)
    }
}
