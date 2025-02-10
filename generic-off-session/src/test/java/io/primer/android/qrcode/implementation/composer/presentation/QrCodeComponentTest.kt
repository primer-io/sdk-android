package io.primer.android.qrcode.implementation.composer.presentation

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.CoroutineScopeProvider
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.payments.core.helpers.PollingStartHandler
import io.primer.android.payments.core.status.domain.AsyncPaymentMethodPollingInteractor
import io.primer.android.payments.core.status.domain.model.AsyncStatus
import io.primer.android.payments.core.status.domain.model.AsyncStatusParams
import io.primer.android.qrcode.implementation.payment.delegate.QrCodePaymentDelegate
import io.primer.android.qrcode.implementation.tokenization.presentation.QrCodeTokenizationDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.ConcurrentHashMap

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class QrCodeComponentTest {
    private lateinit var component: QrCodeComponent
    private val tokenizationDelegate: QrCodeTokenizationDelegate = mockk(relaxed = true)
    private val pollingInteractor: AsyncPaymentMethodPollingInteractor = mockk(relaxed = true)
    private val paymentDelegate: QrCodePaymentDelegate = mockk(relaxed = true)
    private val pollingStartHandler: PollingStartHandler = mockk()

    @BeforeEach
    fun setUp() {
        DISdkContext.headlessSdkContainer =
            mockk<SdkContainer>(relaxed = true).also { sdkContainer ->
                val cont =
                    spyk<DependencyContainer>().also { container ->
                        container.registerFactory<CoroutineScopeProvider> {
                            object : CoroutineScopeProvider {
                                override val scope: CoroutineScope
                                    get() = TestScope()
                            }
                        }
                    }
                every { sdkContainer.containers }
                    .returns(ConcurrentHashMap(mutableMapOf(cont::class.simpleName.orEmpty() to cont)))
            }
        component = QrCodeComponent(tokenizationDelegate, pollingInteractor, paymentDelegate, pollingStartHandler)
        coEvery { pollingStartHandler.startPolling } returns MutableSharedFlow()
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
}
