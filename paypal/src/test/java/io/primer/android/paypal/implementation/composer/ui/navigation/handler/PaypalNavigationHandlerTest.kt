package io.primer.android.paypal.implementation.composer.ui.navigation.handler

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.primer.android.paypal.implementation.composer.ui.navigation.PaypalNavigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartNewTaskNavigator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class PaypalNavigationHandlerTest {
    private lateinit var paypalNavigationHandler: PaypalNavigationHandler

    @MockK
    private lateinit var mockContext: Context

    @MockK
    private lateinit var mockActivity: Activity

    @MockK
    private lateinit var mockLauncher: ActivityResultLauncher<Intent>

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        paypalNavigationHandler = PaypalNavigationHandler()
    }

    @Test
    fun `getSupportedNavigators with context should return StartNewTaskNavigator`() {
        // Arrange
        // No specific arrangements needed as we are not interacting with mockContext directly

        // Act
        val navigators = paypalNavigationHandler.getSupportedNavigators(mockContext)

        // Assert
        assertEquals(1, navigators.size)
        assertTrue(navigators[0] is StartNewTaskNavigator)
    }

    @Test
    fun `getSupportedNavigators with activity and launcher should return PaypalNavigator`() {
        // Arrange
        // No specific arrangements needed as we are not interacting with mockActivity or mockLauncher directly

        // Act
        val navigators = paypalNavigationHandler.getSupportedNavigators(mockActivity, mockLauncher)

        // Assert
        assertEquals(1, navigators.size)
        assertTrue(navigators[0] is PaypalNavigator)
    }
}
