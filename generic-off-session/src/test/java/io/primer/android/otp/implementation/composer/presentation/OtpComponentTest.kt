package io.primer.android.otp.implementation.composer.presentation

import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.primer.android.analytics.utils.RawDataManagerAnalyticsConstants
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.CoroutineScopeProvider
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.otp.PrimerOtpData
import io.primer.android.otp.implementation.payment.delegate.OtpPaymentDelegate
import io.primer.android.otp.implementation.tokenization.presentation.OtpTokenizationDelegate
import io.primer.android.otp.implementation.tokenization.presentation.composable.OtpTokenizationInputable
import io.primer.android.paymentmethods.CollectableDataValidator
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.core.helpers.PollingStartHandler
import io.primer.android.payments.core.helpers.PollingStartHandler.PollingStartData
import io.primer.android.payments.core.status.domain.AsyncPaymentMethodPollingInteractor
import io.primer.android.payments.core.status.domain.model.AsyncStatus
import io.primer.android.payments.core.status.domain.model.AsyncStatusParams
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.vouchers.InstantExecutorExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class OtpComponentTest {
    val paymentMethodType = "paymentMethodType"
    private val primerSessionIntent = PrimerSessionIntent.CHECKOUT

    private lateinit var component: OtpComponent
    private val tokenizationDelegate: OtpTokenizationDelegate = mockk(relaxed = true)
    private val pollingInteractor: AsyncPaymentMethodPollingInteractor = mockk(relaxed = true)
    private val paymentDelegate: OtpPaymentDelegate = mockk(relaxed = true)
    private val pollingStartHandler: PollingStartHandler = mockk()
    private val collectableDataValidator: CollectableDataValidator<PrimerOtpData> = mockk()
    private val errorMapperRegistry: ErrorMapperRegistry = mockk()
    private val sdkAnalyticsEventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate = mockk()

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
        component = OtpComponent(
            tokenizationDelegate,
            pollingInteractor,
            paymentDelegate,
            pollingStartHandler,
            collectableDataValidator,
            errorMapperRegistry,
            sdkAnalyticsEventLoggingDelegate
        )
        coEvery { pollingStartHandler.startPolling } returns MutableSharedFlow()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `start() starts polling on pollingStartHandler emission`() = runTest {
        val spy = spyk(component)
        every { pollingStartHandler.startPolling } returns flowOf(
            PollingStartData(
                statusUrl = "statusUrl",
                paymentMethodType = paymentMethodType
            )
        )

        spy.start(paymentMethodType = paymentMethodType, sessionIntent = primerSessionIntent)

        delay(1.seconds)

        verify {
            spy.startPolling(url = "statusUrl", paymentMethodType = paymentMethodType)
        }
    }

    @Test
    fun `submit() should call tokenize() and handlePaymentMethodToken() when tokenization is successful`() = runTest {
        every { pollingStartHandler.startPolling } returns flowOf(
            PollingStartData(
                statusUrl = "statusUrl",
                paymentMethodType = paymentMethodType
            )
        )
        coEvery { sdkAnalyticsEventLoggingDelegate.logSdkAnalyticsEvent(any(), any(), any()) } just Runs
        val otpData = PrimerOtpData("123456")
        val primerPaymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>()
        coEvery { tokenizationDelegate.tokenize(any()) } returns Result.success(primerPaymentMethodTokenData)
        val paymentDecision = mockk<PaymentDecision>()
        coEvery { paymentDelegate.handlePaymentMethodToken(any(), any()) } returns Result.success(paymentDecision)
        component.start(paymentMethodType, primerSessionIntent)
        component.updateCollectedData(otpData)
        delay(1.seconds)

        component.submit()
        delay(1.seconds)

        coVerify {
            sdkAnalyticsEventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = RawDataManagerAnalyticsConstants.SET_RAW_DATA_METHOD,
                paymentMethodType = paymentMethodType,
                context = mapOf(
                    RawDataManagerAnalyticsConstants.PAYMENT_METHOD_TYPE_PARAM to paymentMethodType
                )
            )
            tokenizationDelegate.tokenize(
                OtpTokenizationInputable(
                    otpData = otpData,
                    paymentMethodType = paymentMethodType,
                    primerSessionIntent = primerSessionIntent
                )
            )
            paymentDelegate.handlePaymentMethodToken(
                paymentMethodTokenData = primerPaymentMethodTokenData,
                primerSessionIntent = primerSessionIntent
            )
        }
        confirmVerified(paymentDecision)
    }

    @Test
    fun `start() should not call tokenize() when data is not collected`() = runTest {
        component.start(paymentMethodType, primerSessionIntent)
        delay(1.seconds)

        component.submit()
        delay(1.seconds)

        coVerify(exactly = 0) {
            tokenizationDelegate.tokenize(any())
        }
        coVerify { paymentDelegate.handleError(any()) }
    }

    @Test
    fun `submit() should not call tokenize() when component is not started`() = runTest {
        component.submit()
        delay(1.seconds)

        coVerify(exactly = 0) {
            tokenizationDelegate.tokenize(any())
        }
        coVerify { paymentDelegate.handleError(any()) }
    }

    @Test
    fun `start() should not call handlePaymentMethodToken() when tokenization fails`() = runTest {
        every { pollingStartHandler.startPolling } returns flowOf(
            PollingStartData(
                statusUrl = "statusUrl",
                paymentMethodType = paymentMethodType
            )
        )
        coEvery { sdkAnalyticsEventLoggingDelegate.logSdkAnalyticsEvent(any(), any(), any()) } just Runs
        val otpData = PrimerOtpData("123456")
        coEvery { tokenizationDelegate.tokenize(any()) } returns Result.failure(Exception())
        component.start(paymentMethodType, primerSessionIntent)
        component.updateCollectedData(otpData)
        delay(1.seconds)

        component.submit()
        delay(1.seconds)

        coVerify {
            sdkAnalyticsEventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = RawDataManagerAnalyticsConstants.SET_RAW_DATA_METHOD,
                paymentMethodType = paymentMethodType,
                context = mapOf(
                    RawDataManagerAnalyticsConstants.PAYMENT_METHOD_TYPE_PARAM to paymentMethodType
                )
            )
            tokenizationDelegate.tokenize(
                OtpTokenizationInputable(
                    otpData = otpData,
                    paymentMethodType = paymentMethodType,
                    primerSessionIntent = primerSessionIntent
                )
            )
        }
        coVerify(exactly = 0) {
            paymentDelegate.handlePaymentMethodToken(
                paymentMethodTokenData = any(),
                primerSessionIntent = any()
            )
        }
        coVerify { paymentDelegate.handleError(any()) }
    }

    @Test
    fun `start() should handle error when handlePaymentMethodToken() fails`() = runTest {
        every { pollingStartHandler.startPolling } returns flowOf(
            PollingStartData(
                statusUrl = "statusUrl",
                paymentMethodType = paymentMethodType
            )
        )
        coEvery { sdkAnalyticsEventLoggingDelegate.logSdkAnalyticsEvent(any(), any(), any()) } just Runs
        val otpData = PrimerOtpData("123456")
        val primerPaymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>()
        coEvery { tokenizationDelegate.tokenize(any()) } returns Result.success(primerPaymentMethodTokenData)
        coEvery { paymentDelegate.handlePaymentMethodToken(any(), any()) } returns Result.failure(Exception())
        component.start(paymentMethodType, primerSessionIntent)
        component.updateCollectedData(otpData)
        delay(1.seconds)

        component.submit()
        delay(1.seconds)

        coVerify {
            sdkAnalyticsEventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = RawDataManagerAnalyticsConstants.SET_RAW_DATA_METHOD,
                paymentMethodType = paymentMethodType,
                context = mapOf(
                    RawDataManagerAnalyticsConstants.PAYMENT_METHOD_TYPE_PARAM to paymentMethodType
                )
            )
            tokenizationDelegate.tokenize(
                OtpTokenizationInputable(
                    otpData = otpData,
                    paymentMethodType = paymentMethodType,
                    primerSessionIntent = primerSessionIntent
                )
            )
            paymentDelegate.handlePaymentMethodToken(
                paymentMethodTokenData = any(),
                primerSessionIntent = any()
            )
        }
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
    fun `startPolling should invoke pollingInteractor and call handleFailure when failure`() {
        val statusUrl = "testStatusUrl"
        val paymentMethodType = "testPaymentMethod"
        every { pollingInteractor.execute(ofType<AsyncStatusParams>()) } returns flow { throw Exception() }

        runTest {
            component.startPolling(statusUrl, paymentMethodType)
        }

        coVerify { pollingInteractor.execute(ofType<AsyncStatusParams>()) }
        coVerify { paymentDelegate.handleError(any()) }
        coVerify(exactly = 0) { paymentDelegate.resumePayment(any()) }
    }

    @Test
    fun `startPolling should invoke pollingInteractor and call handleFailure when resumePayment fails`() {
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
