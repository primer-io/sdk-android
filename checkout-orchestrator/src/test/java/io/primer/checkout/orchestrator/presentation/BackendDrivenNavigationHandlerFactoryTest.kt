package io.primer.checkout.orchestrator.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.checkout.orchestrator.domain.ui.StepUiHandlerRegistry
import io.primer.paymentMethodCoreUi.core.ui.navigation.Navigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.PaymentMethodContextNavigationHandler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentHashMap

internal class BackendDrivenNavigationHandlerFactoryTest {

    private lateinit var registry: StepUiHandlerRegistry
    private lateinit var factory: BackendDrivenNavigationHandlerFactory

    @BeforeEach
    fun setUp() {
        registry = StepUiHandlerRegistry()

        val container = spyk<DependencyContainer>().also { container ->
            container.registerSingleton { registry }
        }
        DISdkContext.headlessSdkContainer = mockk<SdkContainer>(relaxed = true).also { sdk ->
            every { sdk.containers } returns
                ConcurrentHashMap(mutableMapOf(container::class.simpleName.orEmpty() to container))
        }

        factory = BackendDrivenNavigationHandlerFactory()
    }

    @AfterEach
    fun tearDown() {
        DISdkContext.headlessSdkContainer = null
    }

    @Test
    fun `create should return handler with no navigators when registry is empty`() {
        val handler = factory.create() as PaymentMethodContextNavigationHandler

        val navigators = handler.getSupportedNavigators(mockk<Context>())

        assertTrue(navigators.isEmpty())
    }

    @Test
    fun `create should return handler that delegates to registered navigation handlers`() {
        val navigator = mockk<Navigator<NavigationParams>>()
        val navHandler = mockk<PaymentMethodContextNavigationHandler> {
            every { getSupportedNavigators(any<Activity>(), any<ActivityResultLauncher<Intent>>()) } returns
                listOf(navigator)
        }
        registry.register(
            mockk { every { createNavigationHandler() } returns navHandler },
        )

        val handler = factory.create() as PaymentMethodContextNavigationHandler
        val navigators = handler.getSupportedNavigators(mockk<Activity>(), mockk<ActivityResultLauncher<Intent>>())

        assertEquals(listOf(navigator), navigators)
    }
}
