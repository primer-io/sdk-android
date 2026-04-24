@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.checkout.orchestrator.domain.ui

import android.app.Activity
import android.content.Intent
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.webRedirectShared.implementation.composer.presentation.WebRedirectLauncherParams
import io.primer.android.webRedirectShared.implementation.composer.ui.navigation.launcher.WebRedirectActivityLauncherParams
import io.primer.checkout.orchestrator.domain.ReturnUriProvider
import io.primer.executionengine.domain.handler.UrlOpenHandler
import io.primer.executionengine.domain.handler.UrlOpenLaunchRequest
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import io.primer.paymentMethodCoreUi.core.ui.webview.WebViewActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class UrlOpenStepUiHandlerTest {

    @MockK
    lateinit var urlOpenHandler: UrlOpenHandler

    @MockK
    lateinit var returnUriProvider: ReturnUriProvider

    private val launchRequestFlow = MutableSharedFlow<UrlOpenLaunchRequest>(replay = 1)

    private lateinit var handler: UrlOpenStepUiHandler

    @BeforeEach
    fun setUp() {
        every { urlOpenHandler.launchRequest } returns launchRequestFlow
        coEvery { returnUriProvider.provide() } returns RETURN_URI
        handler = UrlOpenStepUiHandler(urlOpenHandler, returnUriProvider)
    }

    @Test
    fun `observeLaunchRequests should emit Navigate event with correct params`() =
        runTest(UnconfinedTestDispatcher()) {
            val emittedEvents = mutableListOf<ComposerUiEvent>()
            handler.observeLaunchRequests(
                scope = backgroundScope,
                paymentMethodType = PAYMENT_METHOD_TYPE,
                emitter = { emittedEvents.add(it) },
            )

            launchRequestFlow.emit(
                UrlOpenLaunchRequest(
                    url = "https://pay.example.com",
                    redirectUrls = listOf("https://redirect.example.com"),
                    title = "Pay Now",
                ),
            )

            assertEquals(1, emittedEvents.size)
            val navigate = emittedEvents[0] as ComposerUiEvent.Navigate
            val launcherParams = navigate.params as PaymentMethodLauncherParams
            assertEquals(PAYMENT_METHOD_TYPE, launcherParams.paymentMethodType)
            assertEquals(PrimerSessionIntent.CHECKOUT, launcherParams.sessionIntent)

            val redirectParams = launcherParams.initialLauncherParams as WebRedirectLauncherParams
            assertEquals("Pay Now", redirectParams.title)
            assertEquals("https://pay.example.com", redirectParams.redirectUrl)
            assertEquals(RETURN_URI, redirectParams.returnUrl)
            assertEquals(PAYMENT_METHOD_TYPE, redirectParams.paymentMethodType)
            assertEquals("", redirectParams.statusUrl)
        }

    @Test
    fun `observeLaunchRequests should use empty title when title is null`() =
        runTest(UnconfinedTestDispatcher()) {
            val emittedEvents = mutableListOf<ComposerUiEvent>()
            handler.observeLaunchRequests(
                scope = backgroundScope,
                paymentMethodType = PAYMENT_METHOD_TYPE,
                emitter = { emittedEvents.add(it) },
            )

            launchRequestFlow.emit(
                UrlOpenLaunchRequest(url = "https://pay.example.com", redirectUrls = null, title = null),
            )

            val navigate = emittedEvents[0] as ComposerUiEvent.Navigate
            val launcherParams = navigate.params as PaymentMethodLauncherParams
            val redirectParams = launcherParams.initialLauncherParams as WebRedirectLauncherParams
            assertEquals("", redirectParams.title)
        }

    @Test
    fun `handleActivityResult should call onResultOk when RESULT_OK`() {
        every { urlOpenHandler.onResultOk() } returns Unit

        val handled = handler.handleActivityResult(createParams(), Activity.RESULT_OK, null)

        assertTrue(handled)
        verify { urlOpenHandler.onResultOk() }
    }

    @Test
    fun `handleActivityResult should call onResultCancelled when RESULT_CANCELED`() {
        every { urlOpenHandler.onResultCancelled() } returns Unit

        val handled = handler.handleActivityResult(createParams(), Activity.RESULT_CANCELED, null)

        assertTrue(handled)
        verify { urlOpenHandler.onResultCancelled() }
    }

    @Test
    fun `handleActivityResult should call onResultError with data when RESULT_ERROR`() {
        val intent = mockk<Intent> {
            every { dataString } returns "https://error.example.com"
        }
        every { urlOpenHandler.onResultError(any()) } returns Unit

        val handled = handler.handleActivityResult(createParams(), WebViewActivity.RESULT_ERROR, intent)

        assertTrue(handled)
        verify { urlOpenHandler.onResultError("https://error.example.com") }
    }

    @Test
    fun `handleActivityResult should pass empty string when intent data is null on error`() {
        every { urlOpenHandler.onResultError(any()) } returns Unit

        handler.handleActivityResult(createParams(), WebViewActivity.RESULT_ERROR, null)

        verify { urlOpenHandler.onResultError("") }
    }

    @Test
    fun `handleActivityStartEvent should return Navigate with WebRedirectActivityLauncherParams`() {
        val redirectLauncherParams = WebRedirectLauncherParams(
            title = "Payment",
            paymentMethodType = PAYMENT_METHOD_TYPE,
            redirectUrl = "https://pay.example.com",
            statusUrl = "https://status.example.com",
            returnUrl = RETURN_URI,
        )
        val params = PaymentMethodLauncherParams(
            paymentMethodType = PAYMENT_METHOD_TYPE,
            sessionIntent = PrimerSessionIntent.CHECKOUT,
            initialLauncherParams = redirectLauncherParams,
        )

        val event = handler.handleActivityStartEvent(params)

        assertTrue(event is ComposerUiEvent.Navigate)
        val activityParams = (event as ComposerUiEvent.Navigate).params as WebRedirectActivityLauncherParams
        assertEquals("https://status.example.com", activityParams.statusUrl)
        assertEquals("https://pay.example.com", activityParams.paymentUrl)
        assertEquals("Payment", activityParams.title)
        assertEquals(PAYMENT_METHOD_TYPE, activityParams.paymentMethodType)
        assertEquals(RETURN_URI, activityParams.returnUrl)
    }

    @Test
    fun `handleActivityStartEvent should return null when initialLauncherParams is not WebRedirectLauncherParams`() {
        val params = PaymentMethodLauncherParams(
            paymentMethodType = PAYMENT_METHOD_TYPE,
            sessionIntent = PrimerSessionIntent.CHECKOUT,
            initialLauncherParams = null,
        )

        val event = handler.handleActivityStartEvent(params)

        assertNull(event)
    }

    private fun createParams() = PaymentMethodLauncherParams(
        paymentMethodType = PAYMENT_METHOD_TYPE,
        sessionIntent = PrimerSessionIntent.CHECKOUT,
    )

    private companion object {
        const val PAYMENT_METHOD_TYPE = "ADYEN_IDEAL"
        const val RETURN_URI = "primer://requestor.com.example.app/async"
    }
}
