package io.primer.android.di

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class SdkContainerTest {

    private lateinit var sdkContainer: SdkContainer

    @Test
    fun `should register a container when registerContainer() is called`() {
        val expectedContainer = MockContainer()

        whenMockContainerIsRegistered(expectedContainer)

        thenContainersContainExpectedValue(expectedContainer)
    }

    @Test
    fun `should unregister the containers when unregister() is called`() {
        whenMockContainerIsRegistered(MockContainer())

        sdkContainer.unregisterContainer<MockContainer>()

        thenContainersAreEmpty()
    }

    @Test
    fun `should clear the containers when clear() is called`() {
        whenMockContainerIsRegistered(MockContainer())

        sdkContainer.clear()

        thenContainersAreEmpty()
    }

    @Test
    fun `should resole the correct dependency when resolve() is called`() {
        whenMockContainerIsRegistered(MockContainer())

        val expectedDependency: MockDependency = sdkContainer.resolve()

        thenExpectedDependencyIsReturned(expectedDependency)
    }

    @Test
    fun `should resole the correct dependency when resolve() is called with a name`() {
        whenMockContainerIsRegistered(MockContainer())

        val expectedDependency: MockDependency = sdkContainer.resolve(MockContainer.DependencyName)

        thenExpectedDependencyIsReturned(expectedDependency)
    }

    @Test
    fun `should throw an unregistered type error when resolve() can not resolve the dependency`() {
        val expectedMessage = "Unregistered type io.primer.android.di.MockDependency"

        whenMockContainerIsRegistered(MockContainer())

        try {
            whenNonExistingDependencyIsResolved<MockDependency>()
        } catch (e: Exception) {
            thenUnregisteredTypeErrorIsThrown(e, expectedMessage)
        }
    }

    private fun whenMockContainerIsRegistered(container: MockContainer) {
        sdkContainer = SdkContainer().apply {
            registerContainer(container)
        }
    }

    private inline fun <reified T : Any> whenNonExistingDependencyIsResolved() {
        sdkContainer.resolve("NotARegisteredName") as T
    }

    private fun thenContainersContainExpectedValue(expectedContainer: DependencyContainer) {
        assertTrue(sdkContainer.containers.containsValue(expectedContainer))
    }

    private fun thenContainersAreEmpty() {
        assertTrue(sdkContainer.containers.isEmpty())
    }

    private fun thenExpectedDependencyIsReturned(expectedDependency: Any) {
        assertNotNull(expectedDependency)
    }

    private fun thenUnregisteredTypeErrorIsThrown(e: Exception, expectedMessage: String) {
        assertEquals(expectedMessage, e.message)
    }
}
