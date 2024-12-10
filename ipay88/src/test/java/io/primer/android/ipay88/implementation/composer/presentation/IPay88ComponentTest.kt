package io.primer.android.ipay88.implementation.composer.presentation

import android.app.Activity
import android.content.Intent
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.configuration.mock.presentation.MockConfigurationDelegate
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.extensions.getSerializableCompat
import io.primer.android.core.utils.CoroutineScopeProvider
import io.primer.android.ipay88.InstantExecutorExtension
import io.primer.android.ipay88.implementation.composer.ui.navigation.launcher.IPay88ActivityLauncherParams
import io.primer.android.ipay88.implementation.composer.ui.navigation.launcher.IPay88MockActivityLauncherParams
import io.primer.android.ipay88.implementation.payment.presentation.delegate.presentation.IPay88PaymentDelegate
import io.primer.android.ipay88.implementation.payment.resume.handler.IPay88ResumeHandler
import io.primer.android.ipay88.implementation.tokenization.presentation.IPay88TokenizationDelegate
import io.primer.android.ipay88.toListDuring
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.payments.core.status.domain.AsyncPaymentMethodPollingInteractor
import io.primer.android.payments.core.status.domain.model.AsyncStatus
import io.primer.android.payments.core.status.domain.model.AsyncStatusParams
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.ipay88.api.ui.NativeIPay88Activity
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class IPay88ComponentTest {

    private lateinit var component: IPay88Component
    private val tokenizationDelegate: IPay88TokenizationDelegate = mockk(relaxed = true)
    private val pollingInteractor: AsyncPaymentMethodPollingInteractor = mockk(relaxed = true)
    private val paymentDelegate: IPay88PaymentDelegate = mockk(relaxed = true)
    private val mockConfigurationDelegate: MockConfigurationDelegate = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
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
        component = IPay88Component(tokenizationDelegate, pollingInteractor, paymentDelegate, mockConfigurationDelegate)
        coEvery { paymentDelegate.uiEvent } returns MutableSharedFlow()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `start() method should call tokenize() and handlePaymentMethodToken() when tokenization is successful`() {
        val paymentMethodType = "testPaymentMethod"
        val primerPaymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>(relaxed = true)
        coEvery { tokenizationDelegate.tokenize(any()) } returns Result.success(primerPaymentMethodTokenData)

        runTest {
            component.start(paymentMethodType, PrimerSessionIntent.CHECKOUT)
        }

        coVerify { tokenizationDelegate.tokenize(any()) }
        coVerify { paymentDelegate.handlePaymentMethodToken(any(), any()) }
    }

    @Test
    fun `start() method should call tokenize() and handleError() when tokenization fails`() {
        val paymentMethodType = "testPaymentMethod"
        coEvery { tokenizationDelegate.tokenize(any()) } returns Result.failure(Exception())

        runTest {
            component.start(paymentMethodType, PrimerSessionIntent.CHECKOUT)
        }

        coVerify { tokenizationDelegate.tokenize(any()) }
        coVerify { paymentDelegate.handleError(any()) }
    }

    @Test
    fun `handleActivityResultIntent with RESULT_CANCELED should handle PaymentMethodCancelledException`() {
        val params: PaymentMethodLauncherParams = mockk(relaxed = true) {
            every { initialLauncherParams } returns mockk<RedirectLauncherParams>() {
                every { statusUrl } returns "testStatusUrl"
                every { paymentMethodType } returns "testPaymentMethod"
            }
        }

        runTest {
            component.handleActivityResultIntent(params, Activity.RESULT_CANCELED, null)
        }

        coVerify {
            paymentDelegate.handleError(any())
        }
    }

    @Test
    fun `handleActivityResultIntent with RESULT_ERROR_CODE should handle PaymentMethodCancelledException`() {
        val exception: Exception = mockk()
        val params: PaymentMethodLauncherParams = mockk(relaxed = true)
        val intent = mockk<Intent> {
            every { getSerializableCompat<Exception>(NativeIPay88Activity.ERROR_KEY) } returns exception
        }

        runTest {
            component.handleActivityResultIntent(params, IPay88ResumeHandler.RESULT_ERROR_CODE, intent)
        }

        coVerify {
            paymentDelegate.handleError(exception)
        }
    }

    @Test
    fun `handleActivityResultIntent with RESULT_OK should start polling`() {
        val params: PaymentMethodLauncherParams = mockk(relaxed = true) {
            every { initialLauncherParams } returns mockk<RedirectLauncherParams>() {
                every { statusUrl } returns "testStatusUrl"
                every { paymentMethodType } returns "testPaymentMethod"
            }
        }
        val resultCode = Activity.RESULT_OK
        val intent: Intent? = null

        runTest {
            component.handleActivityResultIntent(params, resultCode, intent)
        }

        coVerify {
            pollingInteractor.execute(ofType<AsyncStatusParams>())
        }
    }

    @Test
    fun `handleActivityStartEvent should open redirect screen`() {
        val params: PaymentMethodLauncherParams = mockk(relaxed = true) {
            every { initialLauncherParams } returns mockk<RedirectLauncherParams>(relaxed = true)
        }

        runTest {
            component.handleActivityStartEvent(params)
            val events = component.uiEvent.toListDuring(1.0.seconds)
            Assertions.assertTrue(events.any { it is ComposerUiEvent.Navigate })
        }
    }

    @Test
    fun `startPolling should invoke pollingInteractor and resumePayment when success`() {
        val statusUrl = "testStatusUrl"
        val paymentMethodType = "testPaymentMethod"
        val asyncStatus = AsyncStatus("testResumeToken")
        every { pollingInteractor.execute(ofType<AsyncStatusParams>()) } returns flowOf(asyncStatus)

        runTest {
            component.startPolling(statusUrl, paymentMethodType)
        }

        coVerify { pollingInteractor.execute(ofType<AsyncStatusParams>()) }
        coVerify { paymentDelegate.resumePayment(any()) }
    }

    @Test
    fun `startPolling should invoke pollingInteractor and resumePayment when failure`() {
        val statusUrl = "testStatusUrl"
        val paymentMethodType = "testPaymentMethod"
        val asyncStatus = AsyncStatus("testResumeToken")
        every { pollingInteractor.execute(ofType<AsyncStatusParams>()) } returns flowOf(asyncStatus)
        coEvery { paymentDelegate.resumePayment(any()) } throws Exception()

        runTest {
            component.startPolling(statusUrl, paymentMethodType)
        }

        coVerify { pollingInteractor.execute(ofType<AsyncStatusParams>()) }
        coVerify { paymentDelegate.resumePayment(any()) }
        coVerify { paymentDelegate.handleError(any()) }
    }

    @Test
    fun `openRedirectScreen should emit Navigate event when not in mocked flow`() {
        val event: RedirectLauncherParams = mockk(relaxed = true)
        every { mockConfigurationDelegate.isMockedFlow() } returns false

        runTest {
            // When
            component.openRedirectScreen(event)
            // Then
            val events = component.uiEvent.toListDuring(1.0.seconds)
            Assertions.assertTrue(
                events.any { it is ComposerUiEvent.Navigate && it.params is IPay88ActivityLauncherParams }
            )
            verify { mockConfigurationDelegate.isMockedFlow() }
        }
    }

    @Test
    fun `openRedirectScreen should emit Navigate event when in mocked flow`() {
        val event: RedirectLauncherParams = mockk(relaxed = true)
        every { mockConfigurationDelegate.isMockedFlow() } returns true

        runTest {
            // When
            component.openRedirectScreen(event)
            // Then
            val events = component.uiEvent.toListDuring(1.0.seconds)
            Assertions.assertTrue(
                events.any { it is ComposerUiEvent.Navigate && it.params is IPay88MockActivityLauncherParams }
            )
            verify { mockConfigurationDelegate.isMockedFlow() }
        }

        @Test
        fun `close should emit Finish event`() = runTest {
            component.close()
            val events = component.uiEvent.toListDuring(1.0.seconds)
            assertTrue(events.any { it == ComposerUiEvent.Finish })
        }
    }
}
