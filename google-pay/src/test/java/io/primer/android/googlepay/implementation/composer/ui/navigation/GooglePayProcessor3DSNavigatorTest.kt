package io.primer.android.googlepay.implementation.composer.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.android.processor3ds.ui.Processor3dsWebViewActivity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GooglePayProcessor3DSNavigatorTest {
    private lateinit var activity: Activity
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var navigator: GooglePayProcessor3DSNavigator

    @BeforeEach
    fun setUp() {
        activity =
            mockk(relaxed = true) {
                every { applicationContext } returns mockk(relaxed = true)
            }
        launcher = mockk(relaxed = true)
        navigator = GooglePayProcessor3DSNavigator(activity, launcher)

        // Mock the getLaunchIntent method
        mockkObject(Processor3dsWebViewActivity)
    }

    @Test
    fun `navigate() should launch Processor3dsWebViewActivity with correct parameters`() {
        // Given
        val params =
            GooglePayProcessor3DSActivityLauncherParams(
                paymentMethodType = "google_pay",
                redirectUrl = "https://www.example.com/redirect",
                statusUrl = "https://www.example.com/status",
                title = "3DS",
            )
        val expectedIntent = mockk<Intent>()
        every { Processor3dsWebViewActivity.getLaunchIntent(any(), any(), any(), any(), any()) } returns expectedIntent

        // Capture the intent passed to launcher.launch
        val slot = slot<Intent>()
        every { launcher.launch(capture(slot)) } just Runs

        // When
        navigator.navigate(params)

        // Then
        verify { launcher.launch(expectedIntent) }
        assert(slot.captured == expectedIntent)
    }

    @Test
    fun `canHandle() should return true when the params received are of type GooglePayProcessor3DSActivityLauncherParams`() {
        // Given
        val params =
            GooglePayProcessor3DSActivityLauncherParams(
                paymentMethodType = "google_pay",
                redirectUrl = "https://www.example.com/redirect",
                statusUrl = "https://www.example.com/status",
                title = "3DS",
            )

        // When
        val result = navigator.canHandle(params)

        // Then
        assert(result)
    }

    @Test
    fun `canHandle() should return false when the params received are not GooglePayProcessor3DSActivityLauncherParams`() {
        // Given
        val params = mockk<NavigationParams>()

        // When
        val result = navigator.canHandle(params)

        // Then
        assert(!result)
    }
}
