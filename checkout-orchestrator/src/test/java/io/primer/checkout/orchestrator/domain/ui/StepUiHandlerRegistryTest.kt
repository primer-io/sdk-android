package io.primer.checkout.orchestrator.domain.ui

import io.mockk.every
import io.mockk.mockk
import io.primer.paymentMethodCoreUi.core.ui.navigation.PaymentMethodContextNavigationHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class StepUiHandlerRegistryTest {

    private lateinit var registry: StepUiHandlerRegistry

    @BeforeEach
    fun setUp() {
        registry = StepUiHandlerRegistry()
    }

    @Test
    fun `createHandlers should return empty list when no factories registered`() {
        assertEquals(emptyList<StepUiHandler>(), registry.createHandlers())
    }

    @Test
    fun `createHandlers should return handler from registered factory`() {
        val handler = mockk<StepUiHandler>()
        val factory = mockk<StepUiHandlerFactory> {
            every { create() } returns handler
        }

        registry.register(factory)

        assertEquals(listOf(handler), registry.createHandlers())
    }

    @Test
    fun `createHandlers should return handlers from multiple factories in order`() {
        val handler1 = mockk<StepUiHandler>()
        val handler2 = mockk<StepUiHandler>()
        val factory1 = mockk<StepUiHandlerFactory> {
            every { create() } returns handler1
        }
        val factory2 = mockk<StepUiHandlerFactory> {
            every { create() } returns handler2
        }

        registry.register(factory1)
        registry.register(factory2)

        assertEquals(listOf(handler1, handler2), registry.createHandlers())
    }

    @Test
    fun `createNavigationHandlers should return empty list when no factories registered`() {
        assertTrue(registry.createNavigationHandlers().isEmpty())
    }

    @Test
    fun `createNavigationHandlers should skip factories with null navigation handler`() {
        val factory = mockk<StepUiHandlerFactory> {
            every { createNavigationHandler() } returns null
        }

        registry.register(factory)

        assertTrue(registry.createNavigationHandlers().isEmpty())
    }

    @Test
    fun `createNavigationHandlers should return navigation handlers from registered factories`() {
        val navHandler = mockk<PaymentMethodContextNavigationHandler>()
        val factory = mockk<StepUiHandlerFactory> {
            every { createNavigationHandler() } returns navHandler
        }

        registry.register(factory)

        assertEquals(listOf(navHandler), registry.createNavigationHandlers())
    }

    @Test
    fun `createNavigationHandlers should filter out nulls from mixed factories`() {
        val navHandler = mockk<PaymentMethodContextNavigationHandler>()
        val factoryWithNav = mockk<StepUiHandlerFactory> {
            every { createNavigationHandler() } returns navHandler
        }
        val factoryWithoutNav = mockk<StepUiHandlerFactory> {
            every { createNavigationHandler() } returns null
        }

        registry.register(factoryWithoutNav)
        registry.register(factoryWithNav)

        assertEquals(listOf(navHandler), registry.createNavigationHandlers())
    }
}
