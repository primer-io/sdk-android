package io.primer.android.card.implementation.composer.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.android.threeds.ui.ThreeDsActivity
import io.primer.android.threeds.ui.launcher.ThreeDsActivityLauncherParams
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class Card3DSNavigatorTest {
    private lateinit var activityContext: Activity
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var navigator: Card3DSNavigator

    @BeforeEach
    fun setUp() {
        activityContext = mockk(relaxed = true)
        launcher = mockk(relaxed = true)
        navigator = Card3DSNavigator(activityContext, launcher)
        mockkObject(ThreeDsActivity.Companion)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `navigate should call launcher launch() with the correct intent`() {
        // Arrange
        val threeDsParams =
            CardNative3DSActivityLauncherParams(
                paymentMethodType = "card",
                supportedThreeDsVersions = listOf("2.0.0", "2.1.0"),
            )
        val intent = mockk<Intent>()
        every {
            ThreeDsActivity.Companion.getLaunchIntent(
                context = activityContext,
                params =
                    ThreeDsActivityLauncherParams(
                        supportedThreeDsProtocolVersions = threeDsParams.supportedThreeDsVersions,
                    ),
            )
        } returns intent
        every { launcher.launch(intent) } just runs

        // Act
        navigator.navigate(threeDsParams)

        // Assert
        verify {
            launcher.launch(intent)
        }
    }

    @Test
    fun `navigate should launch ThreeDsActivity with specified parameters`() {
        // Arrange
        val params =
            CardNative3DSActivityLauncherParams(
                paymentMethodType = "card",
                supportedThreeDsVersions = listOf("2.0.0", "2.1.0"),
            )
        val threeDsLauncherParams =
            ThreeDsActivityLauncherParams(
                supportedThreeDsProtocolVersions = params.supportedThreeDsVersions,
            )
        val expectedIntent = mockk<Intent>(relaxed = true)

        every {
            ThreeDsActivity.Companion.getLaunchIntent(
                activityContext,
                threeDsLauncherParams,
            )
        } returns expectedIntent

        // Capture the intent passed to launcher.launch
        val slot = slot<Intent>()
        every { launcher.launch(capture(slot)) } just Runs

        // When
        navigator.navigate(params)

        // Then
        verify { launcher.launch(expectedIntent) }
        assert(slot.captured == expectedIntent)

        every { launcher.launch(capture(slot)) } just Runs

        verify {
            ThreeDsActivity.getLaunchIntent(
                eq(activityContext),
                eq(threeDsLauncherParams),
            )
        }
    }

    @Test
    fun `canHandle should return true for CardNative3DSActivityLauncherParams`() {
        // Arrange
        val params =
            CardNative3DSActivityLauncherParams(
                paymentMethodType = "card",
                supportedThreeDsVersions = listOf("2.0.0", "2.1.0"),
            )

        // Act
        val result = navigator.canHandle(params)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `canHandle should return false for non CardNative3DSActivityLauncherParams`() {
        // Arrange
        val params = mockk<NavigationParams>()

        // Act
        val result = navigator.canHandle(params)

        // Assert
        assertTrue(!result)
    }
}
