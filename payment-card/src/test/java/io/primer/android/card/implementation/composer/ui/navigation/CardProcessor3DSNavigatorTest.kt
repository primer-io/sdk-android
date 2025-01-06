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
import io.primer.android.processor3ds.ui.Processor3dsWebViewActivity
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CardProcessor3DSNavigatorTest {
    private lateinit var activityContext: Activity
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var navigator: CardProcessor3DSNavigator

    @BeforeEach
    fun setUp() {
        activityContext = mockk(relaxed = true)
        launcher = mockk(relaxed = true)
        navigator = CardProcessor3DSNavigator(activityContext, launcher)
        mockkObject(Processor3dsWebViewActivity.Companion)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `navigate should call launcher launch() with the correct intent`() {
        // Arrange

        val params =
            CardProcessor3DSActivityLauncherParams(
                paymentMethodType = "card",
                redirectUrl = "https://example.com",
                statusUrl = "https://example.com/status",
                title = "Test title",
            )
        val intent = mockk<Intent>()
        every {
            Processor3dsWebViewActivity.Companion.getLaunchIntent(
                context = activityContext,
                paymentUrl = params.redirectUrl,
                statusUrl = params.statusUrl,
                title = params.title,
                paymentMethodType = params.paymentMethodType,
            )
        } returns intent
        every { launcher.launch(intent) } just runs

        // Act
        navigator.navigate(params)

        // Assert
        verify {
            launcher.launch(intent)
        }
    }

    @Test
    fun `navigate should launch Processor3dsWebViewActivity with specified parameters`() {
        // Arrange
        val params =
            CardProcessor3DSActivityLauncherParams(
                paymentMethodType = "card",
                redirectUrl = "https://example.com",
                statusUrl = "https://example.com/status",
                title = "Test title",
            )
        val expectedIntent = mockk<Intent>(relaxed = true)

        every {
            Processor3dsWebViewActivity.Companion.getLaunchIntent(
                context = activityContext,
                paymentUrl = params.redirectUrl,
                statusUrl = params.statusUrl,
                title = params.title,
                paymentMethodType = params.paymentMethodType,
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
            Processor3dsWebViewActivity.getLaunchIntent(
                eq(activityContext),
                eq(params.redirectUrl),
                eq(params.statusUrl),
                eq(params.title),
                eq(params.paymentMethodType),
            )
        }
    }

    @Test
    fun `canHandle should return true for CardProcessor3DSActivityLauncherParams`() {
        // Arrange
        val params =
            CardProcessor3DSActivityLauncherParams(
                paymentMethodType = "card",
                redirectUrl = "https://example.com",
                statusUrl = "https://example.com/status",
                title = "Test title",
            )

        // Act
        val result = navigator.canHandle(params)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `canHandle should return false for non CardProcessor3DSActivityLauncherParams`() {
        // Arrange
        val params = mockk<NavigationParams>()

        // Act
        val result = navigator.canHandle(params)

        // Assert
        assertTrue(!result)
    }
}
