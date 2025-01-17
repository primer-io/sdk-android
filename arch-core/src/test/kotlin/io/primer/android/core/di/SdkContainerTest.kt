package io.primer.android.core.di

import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.RepetitionInfo
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread
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

        val expectedDependency: MockDependency = sdkContainer.resolve(MockContainer.Companion.DEPENDENCY_NAME)

        thenExpectedDependencyIsReturned(expectedDependency)
    }

    @Test
    fun `should throw an unregistered type error when resolve() can not resolve the dependency`() {
        val expectedMessage =
            "Unable to resolve type io.primer.android.core.di.MockDependency with dependency chain:" +
                " Unregistered type class io.primer.android.core.di.MockDependency for a key NotARegisteredName"

        whenMockContainerIsRegistered(MockContainer())

        try {
            whenNonExistingDependencyIsResolved<MockDependency>()
        } catch (e: Exception) {
            thenUnregisteredTypeErrorIsThrown(e, expectedMessage)
        }
    }

    @RepeatedTest(100)
    fun `should handle concurrent register and resolve calls safely`(repetitionInfo: RepetitionInfo) {
        val numberOfRepetitions = repetitionInfo.currentRepetition * 20
        // Register initial dependencies
        val mockContainer =
            MockContainer().apply {
                repeat(numberOfRepetitions) { i ->
                    registerFactory("dependency$i") { MockDependency() }
                }
            }

        // Create the container and register an initial dependency
        whenMockContainerIsRegistered(mockContainer)

        val threads = mutableListOf<Thread>()
        val exceptionList = mutableListOf<Exception?>()
        val resolvedDependencies = mutableListOf<MockDependency?>()

        threads +=
            thread {
                repeat(numberOfRepetitions) { i ->
                    try {
                        sdkContainer.registerContainer(
                            i.toString(),
                            MockContainer().apply {
                                registerFactory("dependency-1$i") { MockDependency() }
                            },
                        )
                    } catch (e: Exception) {
                        exceptionList.add(e)
                    }
                }
            }

        threads +=
            thread {
                repeat(numberOfRepetitions) { i ->
                    try {
                        // Resolve initial dependencies while other ones are registered
                        val resolvedDependency = sdkContainer.resolve<MockDependency>("dependency$i")
                        resolvedDependencies.add(resolvedDependency)
                    } catch (e: Exception) {
                        exceptionList.add(e)
                    }
                }
            }

        threads.forEach { it.join() }

        // Assert no exceptions were thrown
        assertTrue(exceptionList.isEmpty(), "Concurrent modification exceptions were thrown: $exceptionList")

        // Assert that some dependencies were resolved successfully
        assertTrue(resolvedDependencies.isNotEmpty(), "No dependencies were resolved successfully.")
        assertTrue(resolvedDependencies.size == numberOfRepetitions, "Some resolved dependencies were null.")
    }

    private fun whenMockContainerIsRegistered(container: MockContainer) {
        sdkContainer =
            SdkContainer().apply {
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

    private fun thenUnregisteredTypeErrorIsThrown(
        e: Exception,
        expectedMessage: String,
    ) {
        assertEquals(expectedMessage, e.message)
    }
}
