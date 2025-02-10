package io.primer.android.phoneNumber.implementation.composer.presentation

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
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.paymentmethods.CollectableDataValidator
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.payments.core.helpers.PollingStartHandler
import io.primer.android.payments.core.status.domain.AsyncPaymentMethodPollingInteractor
import io.primer.android.payments.core.status.domain.model.AsyncStatus
import io.primer.android.payments.core.status.domain.model.AsyncStatusParams
import io.primer.android.phoneNumber.PrimerPhoneNumberData
import io.primer.android.phoneNumber.implementation.payment.delegate.PhoneNumberPaymentDelegate
import io.primer.android.phoneNumber.implementation.tokenization.presentation.PhoneNumberTokenizationDelegate
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
class PhoneNumberComponentTest {
    private lateinit var component: PhoneNumberComponent
    private val tokenizationDelegate: PhoneNumberTokenizationDelegate = mockk(relaxed = true)
    private val pollingInteractor: AsyncPaymentMethodPollingInteractor = mockk(relaxed = true)
    private val paymentDelegate: PhoneNumberPaymentDelegate = mockk(relaxed = true)
    private val pollingStartHandler: PollingStartHandler = mockk()
    private val phoneNumberValidator: CollectableDataValidator<PrimerPhoneNumberData> = mockk(relaxed = true)
    private val analytics: PaymentMethodSdkAnalyticsEventLoggingDelegate = mockk(relaxed = true)
    private val errorMapperRegistry: ErrorMapperRegistry = mockk(relaxed = true)
    private val paymentMethodType = "phoneNumber"

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
        component =
            PhoneNumberComponent(
                tokenizationDelegate,
                pollingInteractor,
                paymentDelegate,
                pollingStartHandler,
                phoneNumberValidator,
                errorMapperRegistry,
                analytics,
            )
        coEvery { pollingStartHandler.startPolling } returns MutableSharedFlow()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `startPolling should invoke pollingInteractor and resumePayment when success`() {
        val statusUrl = "testStatusUrl"
        val asyncStatus = AsyncStatus("testResumeToken")
        every { pollingInteractor.execute(ofType<AsyncStatusParams>()) } returns flowOf(asyncStatus)

        runTest {
            component.start(paymentMethodType, PrimerSessionIntent.CHECKOUT)
            component.startPolling(statusUrl, paymentMethodType)
        }

        coVerify { pollingInteractor.execute(ofType<AsyncStatusParams>()) }
        coVerify { paymentDelegate.resumePayment(any()) }
    }

    @Test
    fun `startPolling should invoke pollingInteractor and resumePayment when failure`() {
        val statusUrl = "testStatusUrl"
        val asyncStatus = AsyncStatus("testResumeToken")
        every { pollingInteractor.execute(ofType<AsyncStatusParams>()) } returns flowOf(asyncStatus)
        coEvery { paymentDelegate.resumePayment(any()) } throws Exception()

        runTest {
            component.start(paymentMethodType, PrimerSessionIntent.CHECKOUT)
            component.startPolling(statusUrl, paymentMethodType)
        }

        coVerify { pollingInteractor.execute(ofType<AsyncStatusParams>()) }
        coVerify { paymentDelegate.resumePayment(any()) }
        coVerify { paymentDelegate.handleError(any()) }
    }

    @Test
    fun `updateCollectedData should emit collectedData and validate`() {
        // Arrange
        val collectedData = mockk<PrimerPhoneNumberData>()

        runTest {
            // Act
            component.start(paymentMethodType, PrimerSessionIntent.CHECKOUT)
            component.updateCollectedData(collectedData)
        }

        // Assert
        coVerify { phoneNumberValidator.validate(collectedData) }
    }
}
