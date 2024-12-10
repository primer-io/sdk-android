package io.primer.bancontact.implementation.composer

import android.app.Activity
import android.content.Intent
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.primer.android.PrimerSessionIntent
import io.primer.android.bancontact.PrimerBancontactCardData
import io.primer.android.bancontact.implementation.composer.AdyenBancontactComponent
import io.primer.android.bancontact.implementation.metadata.domain.BancontactCardDataMetadataRetriever
import io.primer.android.bancontact.implementation.payment.delegate.AdyenBancontactPaymentDelegate
import io.primer.android.bancontact.implementation.tokenization.presentation.AdyenBancontactTokenizationDelegate
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.CoroutineScopeProvider
import io.primer.android.paymentmethods.PaymentInputDataValidator
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.core.status.domain.AsyncPaymentMethodPollingInteractor
import io.primer.android.payments.core.status.domain.model.AsyncStatus
import io.primer.android.payments.core.status.domain.model.AsyncStatusParams
import io.primer.android.webRedirectShared.implementation.composer.presentation.WebRedirectLauncherParams
import io.primer.bancontact.implementation.InstantExecutorExtension
import io.primer.bancontact.implementation.toListDuring
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
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
class AdyenBancontactComponentTest {

    private lateinit var component: AdyenBancontactComponent
    private val tokenizationDelegate: AdyenBancontactTokenizationDelegate = mockk(relaxed = true)
    private val pollingInteractor: AsyncPaymentMethodPollingInteractor = mockk(relaxed = true)
    private val paymentDelegate: AdyenBancontactPaymentDelegate = mockk(relaxed = true)
    private val cardInputValidator: PaymentInputDataValidator<PrimerBancontactCardData> = mockk(relaxed = true)
    private val sdkAnalyticsEventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate = mockk(relaxed = true)
    private val metadataRetriever: BancontactCardDataMetadataRetriever = mockk(relaxed = true)

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

        component = AdyenBancontactComponent(
            tokenizationDelegate,
            pollingInteractor,
            paymentDelegate,
            cardInputValidator,
            metadataRetriever,
            sdkAnalyticsEventLoggingDelegate
        )
        coEvery { paymentDelegate.uiEvent } returns MutableSharedFlow()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `submit should start tokenization and handle success`() {
        // Arrange
        val primerSessionIntent = PrimerSessionIntent.CHECKOUT
        val cardData = mockk<PrimerBancontactCardData>() {
            every { copy(any(), any(), any()) } returns mockk()
        }
        val payment = mockk<Payment>()

        coEvery { sdkAnalyticsEventLoggingDelegate.logSdkAnalyticsEvent(any(), any(), any()) } just Runs
        coEvery { tokenizationDelegate.tokenize(any()) } returns Result.success(mockk())
        coEvery {
            paymentDelegate.handlePaymentMethodToken(
                any(),
                any()
            )
        } returns Result.success(PaymentDecision.Success(payment))

        runTest {
            component.start("testPaymentMethod", primerSessionIntent)
            component.updateCollectedData(cardData)
            advanceUntilIdle()

            // Act
            component.submit()
        }

        // Assert
        coVerify { tokenizationDelegate.tokenize(any()) }
        coVerify { paymentDelegate.handlePaymentMethodToken(any(), any()) }
    }

    @Test
    fun `submit should start tokenization and handle failure`() {
        // Arrange
        val primerSessionIntent = PrimerSessionIntent.CHECKOUT
        val cardData = mockk<PrimerBancontactCardData>() {
            every { copy(any(), any(), any()) } returns mockk()
        }
        val exception = Exception("tokenization failed")
        coEvery { tokenizationDelegate.tokenize(any()) } returns Result.failure(exception)
        coEvery { sdkAnalyticsEventLoggingDelegate.logSdkAnalyticsEvent(any(), any(), any()) } just Runs
        coEvery { paymentDelegate.handlePaymentMethodToken(any(), any()) } returns Result.failure(exception)
        coEvery { paymentDelegate.handleError(exception) } just Runs

        runTest {
            component.start("testPaymentMethod", primerSessionIntent)
            component.updateCollectedData(cardData)
            advanceUntilIdle()

            // Act
            component.submit()
        }

        // Assert
        coVerify { tokenizationDelegate.tokenize(any()) }
        coVerify { paymentDelegate.handleError(exception) }
    }

    @Test
    fun `updateCollectedData should emit collectedData and validate`() {
        // Arrange
        val collectedData = mockk<PrimerBancontactCardData> {
            every { copy(any(), any(), any()) } returns mockk()
        }
        val validationErrors = listOf<PrimerInputValidationError>(mockk())
        coEvery { cardInputValidator.validate(any()) } returns validationErrors

        coEvery { sdkAnalyticsEventLoggingDelegate.logSdkAnalyticsEvent(any(), any(), any()) } just Runs

        runTest {
            // Act
            component.start("testPaymentMethod", PrimerSessionIntent.CHECKOUT)
            component.updateCollectedData(collectedData)
        }

        // Assert
        coVerify { cardInputValidator.validate(collectedData) }
    }

    @Test
    fun `handleActivityResultIntent with RESULT_CANCELED should handle PaymentMethodCancelledException`() {
        val params: PaymentMethodLauncherParams = mockk(relaxed = true) {
            every { initialLauncherParams } returns mockk<WebRedirectLauncherParams>() {
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
    fun `handleActivityResultIntent with RESULT_OK should start polling`() {
        val params: PaymentMethodLauncherParams = mockk(relaxed = true) {
            every { initialLauncherParams } returns mockk<WebRedirectLauncherParams>() {
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
            every { initialLauncherParams } returns mockk<WebRedirectLauncherParams>(relaxed = true)
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
    fun `openRedirectScreen should emit Navigate event`() {
        val event: WebRedirectLauncherParams = mockk(relaxed = true)

        runTest {
            // When
            component.openRedirectScreen(event)
            // Then
            val events = component.uiEvent.toListDuring(1.0.seconds)
            Assertions.assertTrue(events.any { it is ComposerUiEvent.Navigate })
        }
    }

    @Test
    fun `close should emit Finish event`() = runTest {
        component.close()
        val events = component.uiEvent.toListDuring(1.0.seconds)
        assertTrue(events.any { it == ComposerUiEvent.Finish })
    }
}
