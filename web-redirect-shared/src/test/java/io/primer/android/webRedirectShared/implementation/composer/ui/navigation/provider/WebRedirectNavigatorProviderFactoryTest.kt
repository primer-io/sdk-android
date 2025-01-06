package io.primer.android.webRedirectShared.implementation.composer.ui.navigation.provider

import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandler
import io.primer.android.webRedirectShared.implementation.composer.ui.navigation.handler.WebRedirectNavigationHandler
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WebRedirectNavigatorProviderFactoryTest {
    private lateinit var factory: WebRedirectNavigatorProviderFactory

    @BeforeEach
    fun setUp() {
        factory = WebRedirectNavigatorProviderFactory()
    }

    @Test
    fun `create should return an instance of WebRedirectNavigationHandler`() {
        val handler: PaymentMethodNavigationHandler = factory.create()
        assertTrue(handler is WebRedirectNavigationHandler)
    }
}
