package io.primer.android.ipay88.implementation.composer.ui.navigation.provider

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandler
import io.primer.android.ipay88.implementation.composer.ui.navigation.handler.IPay88NavigationHandler
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertTrue

// Extend the class with MockKExtension for mocking support
@ExtendWith(MockKExtension::class)
internal class IPay88NavigatorProviderFactoryTest {

    // Inject the class to be tested
    @InjectMockKs
    lateinit var factory: IPay88NavigatorProviderFactory

    @BeforeEach
    fun setUp() {
        // Initialize MockK annotations
        MockKAnnotations.init(this)
    }

    @Test
    fun `create should return an instance of IPay88NavigationHandler`() {
        // Call the method to be tested
        val handler: PaymentMethodNavigationHandler = factory.create()

        // Verify the result
        assertTrue(handler is IPay88NavigationHandler, "Expected handler to be an instance of IPay88NavigationHandler")
    }
}
