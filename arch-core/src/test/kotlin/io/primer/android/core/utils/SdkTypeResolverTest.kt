package io.primer.android.core.utils

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class SdkTypeResolverTest {

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `resolve returns RN_ANDROID when React Native class is available`() {
        mockkObject(SdkTypeResolver)
        every { SdkTypeResolver["isReactNativeAvailable"]() } returns true

        val result = SdkTypeResolver.resolve()

        assertEquals(SdkType.RN_ANDROID, result)
    }

    @Test
    fun `resolve returns ANDROID_NATIVE when React Native class is not available`() {
        mockkObject(SdkTypeResolver)
        every { SdkTypeResolver["isReactNativeAvailable"]() } returns false

        val result = SdkTypeResolver.resolve()

        assertEquals(SdkType.ANDROID_NATIVE, result)
    }

    @Test
    fun `SdkType enum values are correctly defined`() {
        val values = SdkType.entries.toTypedArray()

        assertEquals(2, values.size)
        assertEquals(SdkType.ANDROID_NATIVE, values[0])
        assertEquals(SdkType.RN_ANDROID, values[1])
    }

    @Test
    fun `SdkType valueOf returns correct enum for valid values`() {
        assertEquals(SdkType.ANDROID_NATIVE, SdkType.valueOf("ANDROID_NATIVE"))
        assertEquals(SdkType.RN_ANDROID, SdkType.valueOf("RN_ANDROID"))
    }

    @Test
    fun `SdkType valueOf throws exception for invalid value`() {
        assertThrows(IllegalArgumentException::class.java) {
            SdkType.valueOf("INVALID_TYPE")
        }
    }

    @Test
    fun `SdkType name property returns correct string`() {
        assertEquals("ANDROID_NATIVE", SdkType.ANDROID_NATIVE.name)
        assertEquals("RN_ANDROID", SdkType.RN_ANDROID.name)
    }

    @Test
    fun `SdkType ordinal property returns correct index`() {
        assertEquals(0, SdkType.ANDROID_NATIVE.ordinal)
        assertEquals(1, SdkType.RN_ANDROID.ordinal)
    }

    @Test
    fun `resolve returns ANDROID_NATIVE in real environment without React Native`() {
        val result = SdkTypeResolver.resolve()
        assertEquals(SdkType.ANDROID_NATIVE, result)
    }
}
