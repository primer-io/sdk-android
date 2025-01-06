package io.primer.android.paypal.implementation.composer.ui.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.example.customtabs.launchCustomTab
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.android.paypal.implementation.composer.ui.navigation.launcher.BrowserLauncherParams
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PaypalNavigatorTest {
    @MockK
    lateinit var mockActivity: Activity

    private lateinit var navigator: PaypalNavigator

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        navigator = PaypalNavigator(mockActivity)
    }

    @Test
    fun `navigate should start custom tabs`() {
        val testUrl = "https://example.com"
        val params =
            mockk<BrowserLauncherParams> {
                every { url } returns testUrl
            }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(testUrl))

        mockkStatic(Activity::launchCustomTab)
        every { mockActivity.intent } returns intent
        every { mockActivity.launchCustomTab(testUrl) } returns Unit

        navigator.navigate(params)

        verify(exactly = 1) {
            intent.putExtra("LAUNCHED_BROWSER", true)
            mockActivity.launchCustomTab(testUrl)
        }
    }

    @Test
    fun `canHandle should return true for BrowserLauncherParams`() {
        val testUrl = "https://example.com"
        val params =
            mockk<BrowserLauncherParams> {
                every { url } returns testUrl
            }
        assertTrue(navigator.canHandle(params))
    }

    @Test
    fun `canHandle should return false for other NavigationParams`() {
        val params = object : NavigationParams {}
        assertFalse(navigator.canHandle(params))
    }
}
