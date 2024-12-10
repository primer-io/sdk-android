package io.primer.android.card.implementation.composer.ui.navigation.provider

import io.primer.android.card.implementation.composer.ui.navigation.handler.CardNavigationHandler
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandler
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandlerFactory
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class CardNavigatorProviderFactoryTest {

    @Test
    fun `create() should return an instance of GooglePayNavigationHandler when invoked`() {
        // Given
        val factory: PaymentMethodNavigationHandlerFactory = CardNavigatorProviderFactory()

        // When
        val handler: PaymentMethodNavigationHandler = factory.create()

        // Then
        assertTrue(handler is CardNavigationHandler)
    }
}
