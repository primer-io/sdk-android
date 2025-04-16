package io.primer.android.paypal.implementation.composer.ui.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import com.example.customtabs.canLaunchCustomTabs
import com.example.customtabs.launchCustomTab
import com.example.customtabs.openInBrowser
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.android.paypal.implementation.composer.ui.navigation.launcher.BrowserLauncherParams
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PaypalNavigatorTest {
    @MockK
    lateinit var activity: Activity

    @MockK
    lateinit var launcher: ActivityResultLauncher<Intent>

    private lateinit var navigator: PaypalNavigator

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        navigator = PaypalNavigator(activity = activity, launcher = launcher)
    }

    @AfterEach
    fun afterEach() {
        confirmVerified(activity, launcher)
    }

    @Test
    fun `navigate should launch custom tabs when custom tabs are enabled`() {
        val testUrl = "https://example.com"
        val params =
            mockk<BrowserLauncherParams> {
                every { url } returns testUrl
            }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(testUrl))

        mockkStatic(Activity::launchCustomTab)
        every { activity.canLaunchCustomTabs() } returns true
        every { activity.intent } returns intent
        every { activity.launchCustomTab(launcher = launcher, url = testUrl) } returns Unit

        navigator.navigate(params)

        verify(exactly = 1) {
            activity.canLaunchCustomTabs()
            activity.launchCustomTab(launcher = launcher, url = testUrl)
        }

        verify(exactly = 0) {
            activity.openInBrowser(url = testUrl)
        }

        unmockkStatic(Activity::launchCustomTab)
    }

    @Test
    fun `navigate should launch browser when custom tabs are not enabled`() {
        val testUrl = "https://example.com"
        val params =
            mockk<BrowserLauncherParams> {
                every { url } returns testUrl
            }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(testUrl))

        mockkStatic(Activity::openInBrowser)
        every { activity.canLaunchCustomTabs() } returns false
        every { activity.openInBrowser(any()) } just Runs
        every { activity.intent } returns intent

        navigator.navigate(params)

        verify(exactly = 1) {
            intent.putExtra("LAUNCHED_BROWSER", true)
            activity.canLaunchCustomTabs()
            activity.openInBrowser(url = testUrl)
            activity.intent
        }

        verify(exactly = 0) {
            activity.launchCustomTab(launcher = launcher, url = testUrl)
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
