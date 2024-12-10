package io.primer.android.paypal.implementation.composer.ui.navigation.provider

import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandler
import io.primer.android.paypal.implementation.composer.ui.navigation.handler.PaypalNavigationHandler
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class PaypalNavigatorProviderFactoryTest {

    @Test
    fun `test create method returns PaypalNavigationHandler`() {
        // Arrange
        val factory = PaypalNavigatorProviderFactory()

        // Act
        val navigationHandler: PaymentMethodNavigationHandler = factory.create()

        // Assert
        assertTrue(navigationHandler is PaypalNavigationHandler, "Expected instance of PaypalNavigationHandler")
    }
}
