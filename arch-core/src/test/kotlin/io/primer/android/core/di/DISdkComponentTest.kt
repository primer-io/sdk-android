package io.primer.android.core.di

import io.mockk.spyk
import io.primer.android.core.di.exception.SdkContainerUninitializedException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DISdkComponentTest {
    private val diSdkComponent = object : DISdkComponent {}

    @BeforeEach
    fun setup() {
        DISdkContext.clear()
    }

    @AfterEach
    fun teardown() {
        DISdkContext.clear()
    }

    @Test
    fun `getSdkContainer() throws SdkContainerUninitializedException when dropInSdkContainer and coreSdkContainer are null and isDropIn is true`() {
        DISdkContext.isDropIn = true
        DISdkContext.dropInSdkContainer = null

        assertThrows<SdkContainerUninitializedException> {
            diSdkComponent.getSdkContainer()
        }
    }

    @Test
    fun `getSdkContainer() throws SdkContainerUninitializedException when headlessSdkContainer and coreSdkContainer are null and isDropIn is false`() {
        DISdkContext.isDropIn = false
        DISdkContext.headlessSdkContainer = null

        assertThrows<SdkContainerUninitializedException> {
            diSdkComponent.getSdkContainer()
        }
    }

    @Test
    fun `getSdkContainer() throws SdkContainerUninitializedException when headlessSdkContainer is null and coreSdkContainer is empty and isDropIn is false`() {
        DISdkContext.isDropIn = false
        DISdkContext.headlessSdkContainer = null

        val sdkContainer = spyk<SdkContainer>()
        DISdkContext.coreContainer = sdkContainer

        assertThrows<SdkContainerUninitializedException> {
            diSdkComponent.getSdkContainer()
        }
    }

    @Test
    fun `getSdkContainer() returns merged containers of dropInSdkContainer and coreSdkContainer when isDropIn is true`() {
        DISdkContext.isDropIn = true
        val sdkContainer = spyk<SdkContainer>()
        sdkContainer.registerContainer(spyk<DependencyContainer>())
        DISdkContext.dropInSdkContainer = sdkContainer

        val result = diSdkComponent.getSdkContainer()

        assertEquals(DISdkContext.dropInSdkContainer?.containers, result.containers)
    }

    @Test
    fun `getSdkContainer() returns merged containers of headlessSdkContainer and coreSdkContainer when isDropIn is false`() {
        DISdkContext.isDropIn = false
        val sdkContainer = spyk<SdkContainer>()
        sdkContainer.registerContainer(spyk<DependencyContainer>())
        DISdkContext.headlessSdkContainer = sdkContainer

        val result = diSdkComponent.getSdkContainer()

        assertEquals(DISdkContext.headlessSdkContainer?.containers, result.containers)
    }
}
