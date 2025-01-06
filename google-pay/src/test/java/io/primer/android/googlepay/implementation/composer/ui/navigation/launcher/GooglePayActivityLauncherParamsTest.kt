package io.primer.android.googlepay.implementation.composer.ui.navigation.launcher

import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.configuration.domain.model.CheckoutModule
import io.primer.android.data.settings.PrimerGoogleShippingAddressParameters
import io.primer.android.googlepay.GooglePayFacade
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GooglePayActivityLauncherParamsTest {
    @Test
    fun `test initialization and property access`() {
        // Given
        val environment = GooglePayFacade.Environment.TEST
        val gatewayMerchantId = "merchant_id"
        val merchantName = "Merchant"
        val totalPrice = "100.0"
        val countryCode = "US"
        val currencyCode = "USD"
        val allowedCardNetworks = listOf("VISA", "MASTERCARD")
        val allowedCardAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
        val billingAddressRequired = true
        val shippingOptions = mockk<CheckoutModule.Shipping>()
        val shippingAddressParameters = mockk<PrimerGoogleShippingAddressParameters>()
        val requireShippingMethod = false
        val emailAddressRequired = false

        // When
        val params =
            GooglePayActivityLauncherParams(
                environment = environment,
                gatewayMerchantId = gatewayMerchantId,
                merchantName = merchantName,
                totalPrice = totalPrice,
                countryCode = countryCode,
                currencyCode = currencyCode,
                allowedCardNetworks = allowedCardNetworks,
                allowedCardAuthMethods = allowedCardAuthMethods,
                billingAddressRequired = billingAddressRequired,
                shippingOptions = shippingOptions,
                shippingAddressParameters = shippingAddressParameters,
                requireShippingMethod = requireShippingMethod,
                emailAddressRequired = emailAddressRequired,
            )

        // Then
        assertEquals(environment, params.environment)
        assertEquals(gatewayMerchantId, params.gatewayMerchantId)
        assertEquals(merchantName, params.merchantName)
        assertEquals(totalPrice, params.totalPrice)
        assertEquals(countryCode, params.countryCode)
        assertEquals(currencyCode, params.currencyCode)
        assertEquals(allowedCardNetworks, params.allowedCardNetworks)
        assertEquals(allowedCardAuthMethods, params.allowedCardAuthMethods)
        assertEquals(billingAddressRequired, params.billingAddressRequired)
        assertEquals(shippingOptions, params.shippingOptions)
        assertEquals(shippingAddressParameters, params.shippingAddressParameters)
        assertEquals(requireShippingMethod, params.requireShippingMethod)
        assertEquals(emailAddressRequired, params.emailAddressRequired)

        // Additional checks for the superclass properties
        assertEquals(PaymentMethodType.GOOGLE_PAY.name, params.paymentMethodType)
        assertEquals(PrimerSessionIntent.CHECKOUT, params.sessionIntent)
    }
}
