package io.primer.android.components.analytics.data.provider

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeviceInfoProviderTest {

    private lateinit var context: Context
    private lateinit var resources: Resources
    private lateinit var configuration: Configuration
    private lateinit var provider: DeviceInfoProvider

    @BeforeEach
    fun setUp() {
        context = mockk()
        resources = mockk()
        configuration = Configuration()

        every { context.resources } returns resources
        every { resources.configuration } returns configuration

        provider = DeviceInfoProvider(context)
    }

    @Test
    fun `getDevice returns manufacturer and model`() {
        val result = provider.getDevice()

        // Just verify the format is correct with actual device values
        assertEquals("${Build.MANUFACTURER} ${Build.MODEL}", result)
    }

    @Test
    fun `getDevice returns non-empty string`() {
        val result = provider.getDevice()

        // At minimum, should have a space between manufacturer and model
        assert(result.isNotEmpty())
        assert(result.contains(" "))
    }

    @Test
    fun `getDeviceType returns tablet for large screen`() {
        configuration.screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE

        val result = provider.getDeviceType()

        assertEquals("tablet", result)
    }

    @Test
    fun `getDeviceType returns tablet for xlarge screen`() {
        configuration.screenLayout = Configuration.SCREENLAYOUT_SIZE_XLARGE

        val result = provider.getDeviceType()

        assertEquals("tablet", result)
    }

    @Test
    fun `getDeviceType returns phone for normal screen`() {
        configuration.screenLayout = Configuration.SCREENLAYOUT_SIZE_NORMAL

        val result = provider.getDeviceType()

        assertEquals("phone", result)
    }

    @Test
    fun `getDeviceType returns phone for small screen`() {
        configuration.screenLayout = Configuration.SCREENLAYOUT_SIZE_SMALL

        val result = provider.getDeviceType()

        assertEquals("phone", result)
    }

    @Test
    fun `getUserLocale returns non-empty language tag`() {
        // Just verify that it returns a non-empty string
        // without mocking static Locale methods
        val result = provider.getUserLocale()

        assert(result.isNotEmpty())
    }

    @Test
    fun `getUserLocale returns properly formatted language tag`() {
        // Verify the format without mocking
        val result = provider.getUserLocale()

        // Language tags should contain letters and may contain hyphens
        assert(result.matches(Regex("[a-zA-Z]+(-[a-zA-Z]+)*")))
    }
}
