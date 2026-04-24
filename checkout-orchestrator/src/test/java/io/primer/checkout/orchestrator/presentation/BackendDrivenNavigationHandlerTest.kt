package io.primer.checkout.orchestrator.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.mockk.every
import io.mockk.mockk
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.paymentMethodCoreUi.core.ui.navigation.Navigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.PaymentMethodContextNavigationHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class BackendDrivenNavigationHandlerTest {

    @Test
    fun `getSupportedNavigators with context should aggregate navigators from all handlers`() {
        val context = mockk<Context>()
        val navigator1 = mockk<Navigator<NavigationParams>>()
        val navigator2 = mockk<Navigator<NavigationParams>>()
        val handler1 = mockk<PaymentMethodContextNavigationHandler> {
            every { getSupportedNavigators(context) } returns listOf(navigator1)
        }
        val handler2 = mockk<PaymentMethodContextNavigationHandler> {
            every { getSupportedNavigators(context) } returns listOf(navigator2)
        }

        val compositeHandler = BackendDrivenNavigationHandler(listOf(handler1, handler2))

        assertEquals(listOf(navigator1, navigator2), compositeHandler.getSupportedNavigators(context))
    }

    @Test
    fun `getSupportedNavigators with activity should aggregate navigators from all handlers`() {
        val activity = mockk<Activity>()
        val launcher = mockk<ActivityResultLauncher<Intent>>()
        val navigator1 = mockk<Navigator<NavigationParams>>()
        val navigator2 = mockk<Navigator<NavigationParams>>()
        val handler1 = mockk<PaymentMethodContextNavigationHandler> {
            every { getSupportedNavigators(activity, launcher) } returns listOf(navigator1)
        }
        val handler2 = mockk<PaymentMethodContextNavigationHandler> {
            every { getSupportedNavigators(activity, launcher) } returns listOf(navigator2)
        }

        val compositeHandler = BackendDrivenNavigationHandler(listOf(handler1, handler2))

        assertEquals(
            listOf(navigator1, navigator2),
            compositeHandler.getSupportedNavigators(activity, launcher),
        )
    }

    @Test
    fun `getSupportedNavigators with context should return empty list when no handlers`() {
        val compositeHandler = BackendDrivenNavigationHandler(emptyList())

        assertTrue(compositeHandler.getSupportedNavigators(mockk<Context>()).isEmpty())
    }

    @Test
    fun `getSupportedNavigators with activity should return empty list when no handlers`() {
        val compositeHandler = BackendDrivenNavigationHandler(emptyList())

        assertTrue(
            compositeHandler.getSupportedNavigators(
                mockk<Activity>(),
                mockk<ActivityResultLauncher<Intent>>(),
            ).isEmpty(),
        )
    }
}
