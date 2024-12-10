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
import io.primer.android.threeds.ui.ThreeDsActivity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GooglePay3DSNavigatorTest {

    private lateinit var activity: Activity
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var navigator: GooglePay3DSNavigator

    @BeforeEach
    fun setUp() {
        activity = mockk(relaxed = true) {
            every { applicationContext } returns mockk(relaxed = true)
        }
        launcher = mockk(relaxed = true)
        navigator = GooglePay3DSNavigator(activity, launcher)

        // Mock the getLaunchIntent method
        mockkObject(ThreeDsActivity.Companion)
    }

    @Test
    fun `navigate() should launch ThreeDsActivity with correct parameters`() {
        // Given
        val supportedThreeDsVersions = listOf("1.0", "2.0", "2.1")
        val params = GooglePayNative3DSActivityLauncherParams(
            paymentMethodType = "google_pay",
            supportedThreeDsVersions = supportedThreeDsVersions
        )
        val expectedIntent = mockk<Intent>()
        every { ThreeDsActivity.Companion.getLaunchIntent(any(), any()) } returns expectedIntent

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
    fun `canHandle() should return true when the params received are of type GooglePayNative3DSActivityLauncherParams`() {
        // Given
        val params = GooglePayNative3DSActivityLauncherParams(
            paymentMethodType = "google_pay",
            supportedThreeDsVersions = emptyList()
        )

        // When
        val result = navigator.canHandle(params)

        // Then
        assert(result)
    }

    @Test
    fun `canHandle() should return false when the params received are not GooglePayNative3DSActivityLauncherParams`() {
        // Given
        val params = mockk<NavigationParams>()

        // When
        val result = navigator.canHandle(params)

        // Then
        assert(!result)
    }
}
