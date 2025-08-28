package io.primer.android.core.extensions

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ContextExtensionsTest {

    @Test
    fun `isNightModeEnabled returns true when night mode is enabled`() {
        // Given
        val mockContext = mockk<Context>()
        val mockResources = mockk<Resources>()
        val mockConfiguration = mockk<Configuration>()

        every { mockContext.resources } returns mockResources
        every { mockResources.configuration } returns mockConfiguration
        mockConfiguration.uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL

        // When
        val result = mockContext.isNightModeEnabled()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isNightModeEnabled returns false when night mode is disabled`() {
        // Given
        val mockContext = mockk<Context>()
        val mockResources = mockk<Resources>()
        val mockConfiguration = mockk<Configuration>()

        every { mockContext.resources } returns mockResources
        every { mockResources.configuration } returns mockConfiguration
        mockConfiguration.uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL

        // When
        val result = mockContext.isNightModeEnabled()

        // Then
        assertFalse(result)
    }
}
