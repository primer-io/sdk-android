package io.primer.android.googlepay.implementation.composer.ui.navigation.provider

import io.primer.android.googlepay.implementation.composer.ui.navigation.handler.GooglePayNavigationHandler
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandler
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandlerFactory
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class GooglePayNavigatorProviderFactoryTest {

    @Test
    fun `create() should return an instance of GooglePayNavigationHandler when invoked`() {
        // Given
        val factory: PaymentMethodNavigationHandlerFactory = GooglePayNavigatorProviderFactory()

        // When
        val handler: PaymentMethodNavigationHandler = factory.create()

        // Then
        assertTrue(handler is GooglePayNavigationHandler)
    }
}
