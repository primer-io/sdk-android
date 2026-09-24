package io.primer.android.core.di

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class DISdkContextTest {
    @AfterEach
    fun tearDown() {
        DISdkContext.headlessSdkContainer = null
        DISdkContext.clear()
    }

    @Test
    fun `isHeadlessInitialized is false when the headless container is null`() {
        DISdkContext.headlessSdkContainer = null

        assertFalse(DISdkContext.isHeadlessInitialized)
    }

    @Test
    fun `isHeadlessInitialized is false when the headless container has no registered containers`() {
        DISdkContext.headlessSdkContainer = SdkContainer()

        assertFalse(DISdkContext.isHeadlessInitialized)
    }

    @Test
    fun `isHeadlessInitialized is true when the headless container has a registered container`() {
        DISdkContext.headlessSdkContainer = SdkContainer().apply { registerContainer(MockContainer()) }

        assertTrue(DISdkContext.isHeadlessInitialized)
    }
}
