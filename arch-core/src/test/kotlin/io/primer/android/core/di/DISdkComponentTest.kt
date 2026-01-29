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
    fun `getSdkContainer() throws SdkContainerUninitializedException when dropInSdkContainer and coreSdkContainer are null and sdkType is DROP_IN`() {
        DISdkContext.integrationContext = DISdkContext.integrationContext.copy(sdkType = SdkType.DROP_IN)
        DISdkContext.dropInSdkContainer = null

        assertThrows<SdkContainerUninitializedException> {
            diSdkComponent.getSdkContainer()
        }
    }

    @Test
    fun `getSdkContainer() throws SdkContainerUninitializedException when headlessSdkContainer and coreSdkContainer are null and sdkType is HEADLESS`() {
        DISdkContext.integrationContext = DISdkContext.integrationContext.copy(sdkType = SdkType.HEADLESS)
        DISdkContext.headlessSdkContainer = null

        assertThrows<SdkContainerUninitializedException> {
            diSdkComponent.getSdkContainer()
        }
    }

    @Test
    fun `getSdkContainer() throws SdkContainerUninitializedException when headlessSdkContainer is null and coreSdkContainer is empty and sdkType is HEADLESS`() {
        DISdkContext.integrationContext = DISdkContext.integrationContext.copy(sdkType = SdkType.HEADLESS)
        DISdkContext.headlessSdkContainer = null

        val sdkContainer = spyk<SdkContainer>()
        DISdkContext.coreContainer = sdkContainer

        assertThrows<SdkContainerUninitializedException> {
            diSdkComponent.getSdkContainer()
        }
    }

    @Test
    fun `getSdkContainer() returns merged containers of dropInSdkContainer and coreSdkContainer when sdkType is DROP_IN`() {
        DISdkContext.integrationContext = DISdkContext.integrationContext.copy(sdkType = SdkType.DROP_IN)
        val sdkContainer = spyk<SdkContainer>()
        sdkContainer.registerContainer(spyk<DependencyContainer>())
        DISdkContext.dropInSdkContainer = sdkContainer

        val result = diSdkComponent.getSdkContainer()

        assertEquals(DISdkContext.dropInSdkContainer?.containers, result.containers)
    }

    @Test
    fun `getSdkContainer() returns merged containers of headlessSdkContainer and coreSdkContainer when sdkType is HEADLESS`() {
        DISdkContext.integrationContext = DISdkContext.integrationContext.copy(sdkType = SdkType.HEADLESS)
        val sdkContainer = spyk<SdkContainer>()
        sdkContainer.registerContainer(spyk<DependencyContainer>())
        DISdkContext.headlessSdkContainer = sdkContainer

        val result = diSdkComponent.getSdkContainer()

        assertEquals(DISdkContext.headlessSdkContainer?.containers, result.containers)
    }
}
