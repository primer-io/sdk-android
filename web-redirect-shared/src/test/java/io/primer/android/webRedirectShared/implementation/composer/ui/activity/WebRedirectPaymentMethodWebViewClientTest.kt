package io.primer.android.webRedirectShared.implementation.composer.ui.activity

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.OnBackPressedDispatcher
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
import io.mockk.verify
import io.primer.paymentMethodCoreUi.core.ui.webview.BaseWebViewClient
import io.primer.paymentMethodCoreUi.core.ui.webview.WebViewActivity
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class WebRedirectPaymentMethodWebViewClientTest {
    private val onBackPressedDispatcherMock = mockk<OnBackPressedDispatcher>()
    private lateinit var webViewClient: WebRedirectPaymentMethodWebViewClient
    private val activity: WebViewActivity =
        mockk(relaxed = true) {
            every { this@mockk.onBackPressedDispatcher } returns onBackPressedDispatcherMock
        }
    private val url = "https://example.com"
    private val returnUrl = "https://return.example.com"

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        webViewClient = spyk(WebRedirectPaymentMethodWebViewClient(activity, url, returnUrl))
        mockkStatic(Uri::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getUrlState should return CANCELLED for cancel query param`() {
        val cancelUrl = "https://example.com/cancel"
        every { Uri.parse(cancelUrl).pathSegments } returns listOf("cancel")
        val state = webViewClient.getUrlState(cancelUrl)
        assert(state == BaseWebViewClient.UrlState.CANCELLED)
    }

    @Test
    fun `getUrlState should return PROCESSING for non-cancel query param`() {
        val processingUrl = "https://example.com/processing"
        every { Uri.parse(processingUrl).pathSegments } returns listOf("processing")
        val state = webViewClient.getUrlState(processingUrl)
        println(state)
        assert(state == BaseWebViewClient.UrlState.PROCESSING)
    }

    @Test
    fun `canCaptureUrl should return true for capture URLs`() {
        val captureUrl = "https://primer.io/static/loading.html"
        assert(webViewClient.canCaptureUrl(captureUrl))
    }

    @Test
    fun `canCaptureUrl should return false for non-capture URLs`() {
        val nonCaptureUrl = "https://example.com/non-capture"
        assert(!webViewClient.canCaptureUrl(nonCaptureUrl))
    }

    @Test
    fun `handleDeepLink should return true and handle intent`() {
        val uri = mockk<Uri>(relaxed = true)
        val intent = mockk<Intent>()
        every { webViewClient.getIntentFromUri(uri) } returns intent
        every { webViewClient.handleIntent(intent) } just Runs

        val result = webViewClient.handleDeepLink(uri)

        assert(result)
        verify { webViewClient.handleIntent(intent) }
    }

    @Test
    fun `cannotHandleIntent should log error`() {
        mockkStatic(Log::class)
        val intent =
            mockk<Intent> {
                every { data } returns Uri.parse("https://example.com/cannot-handle")
            }
        every { Log.e(any(), any()) } returns 0

        webViewClient.cannotHandleIntent(intent)

        verify { Log.e(any(), any()) }
        unmockkStatic(Log::class)
    }

    @Test
    fun `onUrlCaptured should call onBackPressed when URL state is CANCELLED`() {
        val intent = mockk<Intent>()
        every { Uri.parse(any()).pathSegments } returns listOf("cancel")
        every { intent.data } returns Uri.EMPTY
        every { onBackPressedDispatcherMock.onBackPressed() } just Runs

        webViewClient.onUrlCaptured(intent)

        verify { onBackPressedDispatcherMock.onBackPressed() }
    }
}
