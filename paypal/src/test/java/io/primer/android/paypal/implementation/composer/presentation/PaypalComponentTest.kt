package io.primer.android.paypal.implementation.composer.presentation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.CoroutineScopeProvider
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.paypal.InstantExecutorExtension
import io.primer.android.paypal.implementation.payment.presentation.delegate.presentation.PaypalPaymentDelegate
import io.primer.android.paypal.implementation.tokenization.presentation.PaypalTokenizationCollectorDelegate
import io.primer.android.paypal.implementation.tokenization.presentation.PaypalTokenizationCollectorParams
import io.primer.android.paypal.implementation.tokenization.presentation.PaypalTokenizationDelegate
import io.primer.android.paypal.implementation.tokenization.presentation.model.PaypalTokenizationInputable
import io.primer.android.paypal.toListDuring
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.time.Duration.Companion.seconds

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class PaypalComponentTest {

    @RelaxedMockK
    internal lateinit var tokenizationCollectorDelegate: PaypalTokenizationCollectorDelegate

    @RelaxedMockK
    internal lateinit var tokenizationDelegate: PaypalTokenizationDelegate

    @RelaxedMockK
    internal lateinit var paymentDelegate: PaypalPaymentDelegate

    private lateinit var component: PaypalComponent

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        DISdkContext.headlessSdkContainer = mockk<SdkContainer>(relaxed = true).also { sdkContainer ->
            val cont = spyk<DependencyContainer>().also { container ->
                container.registerFactory<CoroutineScopeProvider> {
                    object : CoroutineScopeProvider {
                        override val scope: CoroutineScope
                            get() = TestScope()
                    }
                }
            }
            every { sdkContainer.containers }.returns(mutableMapOf(cont::class.simpleName.orEmpty() to cont))
        }
        component = PaypalComponent(
            tokenizationCollectorDelegate = tokenizationCollectorDelegate,
            tokenizationDelegate = tokenizationDelegate,
            paymentDelegate = paymentDelegate
        )

        coEvery { tokenizationCollectorDelegate.uiEvent } returns MutableSharedFlow()
        mockkStatic(Uri::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Uri::class)
    }

    @Test
    fun `start() should emit PaymentMethodCancelledException when data collection was cancelled`() {
        // given
        every { Uri.parse(any()) } returns mockk(relaxed = true)
        coEvery { tokenizationCollectorDelegate.startDataCollection(any()) } throws CancellationException()

        val params = mockk<PaymentMethodLauncherParams>(relaxed = true) {
            every { paymentMethodType } returns "PAYPAL"
            every { initialLauncherParams } returns mockk<RedirectLauncherParams>(relaxed = true)
        }

        // when
        runTest {
            component.start("PAYPAL", PrimerSessionIntent.CHECKOUT)
        }

        // then
        coVerify { paymentDelegate.handleError(ofType(PaymentMethodCancelledException::class)) }
    }

    @Test
    fun `handleActivityResultIntent() should emit PaymentMethodCancelledException when flow was cancelled`() {
        // given
        every { Uri.parse(any()) } returns mockk(relaxed = true)

        val params = mockk<PaymentMethodLauncherParams>(relaxed = true) {
            every { paymentMethodType } returns "PAYPAL"
            every { initialLauncherParams } returns mockk<RedirectLauncherParams>(relaxed = true)
        }

        val resultCode = Activity.RESULT_CANCELED

        // when
        runTest {
            component.start("PAYPAL", PrimerSessionIntent.CHECKOUT)
            component.handleActivityResultIntent(params = params, resultCode = resultCode, intent = null)
        }

        // then
        coVerify { paymentDelegate.handleError(ofType(PaymentMethodCancelledException::class)) }
    }

    @Test
    fun `handleActivityResultIntent() should emit PaymentMethodCancelledException when tokenization was cancelled`() {
        // given
        every { Uri.parse(any()) } returns mockk(relaxed = true)

        coEvery { tokenizationDelegate.tokenize(any()) } throws CancellationException()

        val params = mockk<PaymentMethodLauncherParams>(relaxed = true) {
            every { paymentMethodType } returns "PAYPAL"
            every { initialLauncherParams } returns mockk<RedirectLauncherParams>(relaxed = true)
        }
        val resultCode = Activity.RESULT_CANCELED

        // when
        runTest {
            component.start("PAYPAL", PrimerSessionIntent.CHECKOUT)
            component.handleActivityResultIntent(params = params, resultCode = resultCode, intent = null)
        }

        // then
        coVerify { paymentDelegate.handleError(ofType(PaymentMethodCancelledException::class)) }
    }

    @Test
    fun `handleActivityResultIntent() should call tokenize with PaypalCheckoutTokenizationInputable when session intent is Checkout`() {
        // given
        val params = mockk<PaymentMethodLauncherParams>(relaxed = true) {
            every { sessionIntent } returns PrimerSessionIntent.CHECKOUT
            every { initialLauncherParams } returns mockk<RedirectLauncherParams>(relaxed = true) {
                every { successUrl } returns "https://success.url"
                every { paymentMethodConfigId } returns "configId"
            }
        }
        val resultCode = Activity.RESULT_OK
        val intent = mockk<Intent>(relaxed = true) {
            every { data } returns Uri.parse("https://success.url?token=sampleToken")
        }

        coEvery { tokenizationDelegate.tokenize(any()) } returns Result.success(mockk(relaxed = true))

        // when
        runTest {
            component.start("PAYPAL", PrimerSessionIntent.CHECKOUT)
            component.handleActivityResultIntent(params = params, resultCode = resultCode, intent = intent)
        }

        // then
        coVerify {
            tokenizationDelegate.tokenize(
                input = ofType(PaypalTokenizationInputable.PaypalCheckoutTokenizationInputable::class)
            )
        }
    }

    @Test
    fun `handleActivityResultIntent() should call handleError when tokenization fails`() {
        // given
        val params = mockk<PaymentMethodLauncherParams>(relaxed = true) {
            every { sessionIntent } returns PrimerSessionIntent.CHECKOUT
            every { initialLauncherParams } returns mockk<RedirectLauncherParams>(relaxed = true) {
                every { successUrl } returns "https://success.url"
                every { paymentMethodConfigId } returns "configId"
            }
        }
        val resultCode = Activity.RESULT_OK
        val intent = mockk<Intent>(relaxed = true) {
            every { data } returns Uri.parse("https://success.url?token=sampleToken")
        }

        coEvery { tokenizationDelegate.tokenize(any()) } returns Result.failure(Exception("Tokenization error"))

        // when
        runTest {
            component.start("PAYPAL", PrimerSessionIntent.CHECKOUT)
            component.handleActivityResultIntent(params = params, resultCode = resultCode, intent = intent)
        }

        // then
        coVerify {
            tokenizationDelegate.tokenize(
                input = ofType(PaypalTokenizationInputable.PaypalCheckoutTokenizationInputable::class)
            )
        }
        coVerify { paymentDelegate.handleError(ofType(Throwable::class)) }
    }

    @Test
    fun `handleActivityResultIntent() should call tokenize with PaypalVaultTokenizationInputable when session intent is Vault`() {
        // given
        val params = mockk<PaymentMethodLauncherParams>(relaxed = true) {
            every { sessionIntent } returns PrimerSessionIntent.VAULT
            every { initialLauncherParams } returns mockk<RedirectLauncherParams>(relaxed = true) {
                every { successUrl } returns "https://success.url"
                every { paymentMethodConfigId } returns "configId"
            }
        }
        val resultCode = Activity.RESULT_OK
        val intent = mockk<Intent>(relaxed = true) {
            every { data } returns Uri.parse("https://success.url?ba_token=sampleBaToken")
        }

        coEvery { tokenizationDelegate.tokenize(any()) } returns Result.success(mockk(relaxed = true))

        // when
        runTest {
            component.start("PAYPAL", PrimerSessionIntent.VAULT)
            component.handleActivityResultIntent(params = params, resultCode = resultCode, intent = intent)
        }

        // then
        coVerify {
            tokenizationDelegate.tokenize(
                input = ofType(PaypalTokenizationInputable.PaypalVaultTokenizationInputable::class)
            )
        }
    }

    @Test
    fun `handleActivityStartEvent() should emit Navigate event for RedirectLauncherParams`() {
        // given
        val redirectLauncherParams = mockk<RedirectLauncherParams>(relaxed = true)
        val params = PaymentMethodLauncherParams(
            paymentMethodType = "PAYPAL",
            sessionIntent = PrimerSessionIntent.CHECKOUT,
            initialLauncherParams = redirectLauncherParams
        )

        runTest {
            // when
            component.start("PAYPAL", PrimerSessionIntent.CHECKOUT)
            component.handleActivityStartEvent(params)

            // then
            val events = component.uiEvent.toListDuring(1.0.seconds)
            assertTrue(events.any { it is ComposerUiEvent.Navigate })
        }
    }

    @Test
    fun `start() should start data collection and handle errors`() {
        // given
        val primerSessionIntent = PrimerSessionIntent.CHECKOUT

        coEvery {
            tokenizationCollectorDelegate.startDataCollection(any())
        } returns Result.failure(Exception("Collection error"))

        // when
        runTest {
            component.start("PAYPAL", primerSessionIntent)
        }

        // then
        coVerify { tokenizationCollectorDelegate.startDataCollection(ofType(PaypalTokenizationCollectorParams::class)) }
        coVerify { paymentDelegate.handleError(any()) }
    }
}
