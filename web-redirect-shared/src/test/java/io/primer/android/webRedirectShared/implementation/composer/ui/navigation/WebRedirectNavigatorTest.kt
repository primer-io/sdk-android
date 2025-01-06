package io.primer.android.webRedirectShared.implementation.composer.ui.navigation

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.android.webRedirectShared.implementation.composer.ui.activity.WebRedirectActivity
import io.primer.android.webRedirectShared.implementation.composer.ui.navigation.launcher.WebRedirectActivityLauncherParams
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class WebRedirectNavigatorTest {
    private lateinit var context: Context
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var webRedirectNavigator: WebRedirectNavigator
    private val paymentUrl = "https://example.com"
    private val returnUrl = "https://example.com/return"
    private val title = "Test title"
    private val paymentMethodType = "Test payment method type"
    private val statusUrl = "https://example.com/status"

    @BeforeEach
    fun setUp() {
        context = mockk(relaxed = true)
        launcher = mockk(relaxed = true)
        webRedirectNavigator = WebRedirectNavigator(context, launcher)
        mockkObject(WebRedirectActivity.Companion)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `navigate should launch AsyncPaymentMethodWebViewActivity with specified parameters`() {
        // Arrange
        val params =
            WebRedirectActivityLauncherParams(
                statusUrl = statusUrl,
                paymentUrl = paymentUrl,
                title = title,
                paymentMethodType = paymentMethodType,
                returnUrl = returnUrl,
            )
        val expectedIntent = mockk<Intent>(relaxed = true)

        every {
            WebRedirectActivity.Companion.getLaunchIntent(
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns expectedIntent

        // Capture the intent passed to launcher.launch
        val slot = slot<Intent>()
        every { launcher.launch(capture(slot)) } just Runs

        // When
        webRedirectNavigator.navigate(params)

        // Then
        verify { launcher.launch(expectedIntent) }
        assert(slot.captured == expectedIntent)

        every { launcher.launch(capture(slot)) } just Runs

        verify {
            WebRedirectActivity.getLaunchIntent(
                eq(context),
                eq(paymentUrl),
                eq(returnUrl),
                eq(title),
                eq(paymentMethodType),
            )
        }
    }

    @Test
    fun `canHandle should return true for WebRedirectActivityLauncherParams`() {
        // Arrange
        val params =
            WebRedirectActivityLauncherParams(
                paymentUrl = "https://example.com",
                returnUrl = "https://example.com/return",
                title = "Test title",
                paymentMethodType = "Test payment method type",
                statusUrl = "https://example.com/status",
            )

        // Act & Assert
        assert(webRedirectNavigator.canHandle(params)) {
            "Expected canHandle to return true for WebRedirectActivityLauncherParams"
        }
    }

    @Test
    fun `canHandle should return false for other NavigationParams`() {
        // Arrange
        val params = mockk<NavigationParams>()

        // Act & Assert
        assert(!webRedirectNavigator.canHandle(params)) {
            "Expected canHandle to return false for other NavigationParams"
        }
    }
}
