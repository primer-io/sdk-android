package io.primer.android.card.implementation.composer

import android.app.Activity
import android.content.Intent
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.card.implementation.payment.delegate.CardPaymentDelegate
import io.primer.android.card.implementation.payment.delegate.ProcessorThreeDsInitialLauncherParams
import io.primer.android.card.implementation.payment.delegate.ThreeDsInitialLauncherParams
import io.primer.android.core.toListDuring
import io.primer.android.core.extensions.getSerializableCompat
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.processor3ds.domain.model.Processor3DS
import io.primer.android.processor3ds.ui.Processor3dsWebViewActivity
import io.primer.android.threeds.ui.ThreeDsActivity
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import io.primer.paymentMethodCoreUi.core.ui.webview.WebViewActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.time.Duration.Companion.seconds

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class VaultedCardComponentTest {
    @RelaxedMockK
    internal lateinit var paymentDelegate: CardPaymentDelegate

    private lateinit var component: VaultedCardComponent
    private lateinit var paymentMethodType: String

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        component =
            VaultedCardComponent(
                paymentDelegate = paymentDelegate,
            )
        paymentMethodType = "CARD"

        coEvery { paymentDelegate.uiEvent } returns MutableSharedFlow()
    }

    @Test
    fun `handleActivityResultIntent() should emit PaymentMethodCancelledException when flow was cancelled`() {
        // given
        val params = mockk<PaymentMethodLauncherParams>(relaxed = true)
        val resultCode = Activity.RESULT_CANCELED

        // when
        runTest {
            component.start(paymentMethodType, PrimerSessionIntent.CHECKOUT)
            component.handleActivityResultIntent(params = params, resultCode = resultCode, intent = null)
        }

        // then
        coVerify { paymentDelegate.handleError(ofType(PaymentMethodCancelledException::class)) }
    }

    @Test
    fun `handleActivityResultIntent() should emit GooglePayException when error result`() {
        // given
        val params = mockk<PaymentMethodLauncherParams>(relaxed = true)
        val resultCode = WebViewActivity.RESULT_ERROR

        val exception = Exception("error")
        val intent =
            mockk<Intent> {
                every { getSerializableCompat<Exception>(Processor3dsWebViewActivity.ERROR_KEY) } returns exception
            }

        // when
        runTest {
            component.start(paymentMethodType, PrimerSessionIntent.CHECKOUT)
            component.handleActivityResultIntent(params = params, resultCode = resultCode, intent = intent)
        }

        // then
        coVerify { paymentDelegate.handleError(exception) }
    }

    @Test
    fun `handleActivityResultIntent() should emit resolved Exception when there is error result from processor 3ds`() {
        // given
        val params = mockk<PaymentMethodLauncherParams>(relaxed = true)
        val resultCode = WebViewActivity.RESULT_ERROR

        val intent = mockk<Intent>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)

        every { intent.getSerializableCompat<Exception>(Processor3dsWebViewActivity.ERROR_KEY) } returns exception

        // when
        runTest {
            component.start(paymentMethodType, PrimerSessionIntent.CHECKOUT)
            component.handleActivityResultIntent(params = params, resultCode = resultCode, intent = intent)
        }

        // then
        coVerify { paymentDelegate.handleError(ofType(Exception::class)) }
    }

    @Test
    fun `handleActivityResultIntent() should call resumePayment with correct resume token when initialLauncherParams is ThreeDsInitialLauncherParams`() {
        // Given
        val resumeToken = "resumeToken"
        val params =
            mockk<PaymentMethodLauncherParams>(relaxed = true) {
                every { initialLauncherParams } returns mockk<ThreeDsInitialLauncherParams>(relaxed = true)
            }
        val resultCode = Activity.RESULT_OK
        val intent =
            mockk<Intent>(relaxed = true) {
                every { getStringExtra(ThreeDsActivity.RESUME_TOKEN_EXTRA_KEY) } returns resumeToken
            }

        // When
        runTest {
            component.start(paymentMethodType, PrimerSessionIntent.CHECKOUT)
            component.handleActivityResultIntent(params = params, resultCode = resultCode, intent = intent)
        }

        // Then
        coVerify { paymentDelegate.resumePayment(resumeToken) }
    }

    @Test
    fun `handleActivityStartEvent() should emit Navigate event for ThreeDsInitialLauncherParams`() {
        // Given
        val threeDsInitialLauncherParams = ThreeDsInitialLauncherParams(listOf("2.0.0", "2.1.0"))
        val params =
            PaymentMethodLauncherParams(
                paymentMethodType = paymentMethodType,
                sessionIntent = PrimerSessionIntent.CHECKOUT,
                initialLauncherParams = threeDsInitialLauncherParams,
            )

        runTest {
            // When
            component.start(paymentMethodType, PrimerSessionIntent.CHECKOUT)
            component.handleActivityStartEvent(params)

            // Then
            val events = component.uiEvent.toListDuring(1.0.seconds)
            Assertions.assertTrue(events.any { it is ComposerUiEvent.Navigate })
        }
    }

    @Test
    fun `handleActivityStartEvent() should emit Navigate event for ProcessorThreeDsInitialLauncherParams`() {
        // Given
        val threeDsInitialLauncherParams =
            ProcessorThreeDsInitialLauncherParams(
                processor3DS =
                Processor3DS(
                    redirectUrl = "https://www.example.com/redirect",
                    statusUrl = "https://www.example.com/status",
                ),
            )
        val params =
            PaymentMethodLauncherParams(
                paymentMethodType = paymentMethodType,
                sessionIntent = PrimerSessionIntent.CHECKOUT,
                initialLauncherParams = threeDsInitialLauncherParams,
            )

        runTest {
            // When
            component.start(paymentMethodType, PrimerSessionIntent.CHECKOUT)
            component.handleActivityStartEvent(params)

            // Then
            val events = component.uiEvent.toListDuring(1.0.seconds)
            Assertions.assertTrue(events.any { it is ComposerUiEvent.Navigate })
        }
    }

    @Test
    fun `handleActivityResultIntent() should call resumePayment with correct resume token when initialLauncherParams is ProcessorThreeDsInitialLauncherParams`() {
        // Given
        val resumeToken = "resumeToken"
        val params =
            mockk<PaymentMethodLauncherParams>(relaxed = true) {
                every { initialLauncherParams } returns mockk<ProcessorThreeDsInitialLauncherParams>(relaxed = true)
            }
        val resultCode = Activity.RESULT_OK
        val intent =
            mockk<Intent>(relaxed = true) {
                every { getStringExtra(Processor3dsWebViewActivity.RESUME_TOKEN_EXTRA_KEY) } returns resumeToken
            }

        // When
        runTest {
            component.start(paymentMethodType, PrimerSessionIntent.CHECKOUT)
            component.handleActivityResultIntent(params = params, resultCode = resultCode, intent = intent)
        }

        // Then
        coVerify { paymentDelegate.resumePayment(resumeToken) }
    }
}
