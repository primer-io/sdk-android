package io.primer.android.processor3ds.ui

import android.content.Intent
import android.net.Uri
import android.util.Log
import io.mockk.Runs
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import io.primer.paymentMethodCoreUi.core.ui.webview.BaseWebViewClient
import io.primer.paymentMethodCoreUi.core.ui.webview.WebViewActivity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class Processor3dsWebViewClientTest {

    private lateinit var activity: WebViewActivity
    private lateinit var processor3dsWebViewClient: Processor3dsWebViewClient

    @BeforeEach
    fun setUp() {
        activity = mockk(relaxed = true)
        processor3dsWebViewClient = spyk(
            Processor3dsWebViewClient(
                activity = activity,
                url = "https://example.com",
                returnUrl = "https://return.com"
            )
        )
    }

    @Test
    fun `getUrlState should return PROCESSING`() {
        val url = "https://example.com"
        val state = processor3dsWebViewClient.getUrlState(url)
        assert(state == BaseWebViewClient.UrlState.PROCESSING)
    }

    @Test
    fun `getCaptureUrl should return the same url`() {
        val url = "https://example.com"
        val captureUrl = processor3dsWebViewClient.getCaptureUrl(url)
        assert(captureUrl == url)
    }

    @Test
    fun `canCaptureUrl should return false`() {
        val url = "https://example.com"
        val canCapture = processor3dsWebViewClient.canCaptureUrl(url)
        assert(!canCapture)
    }

    @Test
    fun `handleDeepLink should return true and call handleIntent when getIntentFromUri returns a valid intent`() {
        val uri = mockk<Uri>()
        val intent = mockk<Intent>(relaxed = true)

        every { processor3dsWebViewClient.getIntentFromUri(uri) } returns intent
        every { processor3dsWebViewClient.handleIntent(intent) } just Runs

        val result = processor3dsWebViewClient.handleDeepLink(uri)

        assert(result)
        verify { processor3dsWebViewClient.handleIntent(intent) }
    }

    @Test
    fun `cannotHandleIntent should call the log`() {
        mockkStatic(Log::class)
        val intent = mockk<Intent>(relaxed = true)

        every { Log.e(any(), any()) } returns 0

        processor3dsWebViewClient.cannotHandleIntent(intent)
        verify { Log.e(any(), any()) }

        unmockkStatic(Log::class)
    }
}
