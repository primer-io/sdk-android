package io.primer.checkout.orchestrator.domain.ui

import io.mockk.mockk
import io.mockk.spyk
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.checkout.orchestrator.domain.ReturnUriProvider
import io.primer.executionengine.domain.handler.UrlOpenHandler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentHashMap

internal class UrlOpenStepUiHandlerFactoryTest {

    private lateinit var factory: UrlOpenStepUiHandlerFactory

    @BeforeEach
    fun setUp() {
        val container = spyk<DependencyContainer>().also { container ->
            container.registerFactory<UrlOpenHandler> { mockk(relaxed = true) }
            container.registerFactory<ReturnUriProvider> { mockk(relaxed = true) }
        }
        DISdkContext.headlessSdkContainer = mockk<SdkContainer>(relaxed = true).also { sdk ->
            io.mockk.every { sdk.containers } returns
                ConcurrentHashMap(mutableMapOf(container::class.simpleName.orEmpty() to container))
        }

        factory = UrlOpenStepUiHandlerFactory()
    }

    @AfterEach
    fun tearDown() {
        DISdkContext.headlessSdkContainer = null
    }

    @Test
    fun `create should return UrlOpenStepUiHandler`() {
        val handler = factory.create()

        assertTrue(handler is UrlOpenStepUiHandler)
    }

    @Test
    fun `createNavigationHandler should return a PaymentMethodContextNavigationHandler`() {
        val handler = factory.createNavigationHandler()

        assertNotNull(handler)
    }
}
