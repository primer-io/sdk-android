package io.primer.android.card.implementation.composer

import android.app.Activity
import android.content.Intent
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.card.InstantExecutorExtension
import io.primer.android.card.implementation.composer.ui.navigation.CardNative3DSActivityLauncherParams
import io.primer.android.card.implementation.composer.ui.navigation.CardProcessor3DSActivityLauncherParams
import io.primer.android.card.implementation.composer.ui.navigation.MockCard3DSActivityLauncherParams
import io.primer.android.card.implementation.payment.delegate.CardPaymentDelegate
import io.primer.android.card.implementation.payment.delegate.ProcessorThreeDsInitialLauncherParams
import io.primer.android.card.implementation.payment.delegate.ThreeDsInitialLauncherParams
import io.primer.android.card.implementation.tokenization.presentation.CardTokenizationDelegate
import io.primer.android.card.toListDuring
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.core.models.card.PrimerCardMetadata
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.mock.presentation.MockConfigurationDelegate
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.extensions.getSerializableCompat
import io.primer.android.core.utils.CoroutineScopeProvider
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.paymentmethods.PaymentInputDataValidator
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.processor3ds.domain.model.Processor3DS
import io.primer.android.processor3ds.ui.Processor3dsWebViewActivity
import io.primer.android.threeds.ui.ThreeDsActivity
import io.primer.cardShared.binData.domain.CardDataMetadataRetriever
import io.primer.cardShared.binData.domain.CardMetadataStateRetriever
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import io.primer.paymentMethodCoreUi.core.ui.webview.WebViewActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.time.Duration.Companion.seconds

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
class CardComponentTest {
    private lateinit var component: CardComponent

    private val tokenizationDelegate: CardTokenizationDelegate = mockk()
    private val paymentDelegate: CardPaymentDelegate = mockk(relaxed = true)
    private val cardDataMetadataRetriever: CardDataMetadataRetriever = mockk()
    private val cardDataMetadataStateRetriever: CardMetadataStateRetriever = mockk()
    private val cardInputDataValidator: PaymentInputDataValidator<PrimerCardData> = mockk()
    private val sdkAnalyticsEventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate = mockk()
    private val mockConfigurationDelegate: MockConfigurationDelegate = mockk()

    private val paymentMethodType = "card"

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
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
                every { sdkContainer.containers }.returns(mutableMapOf(cont::class.simpleName.orEmpty() to cont))
            }
        component =
            CardComponent(
                tokenizationDelegate,
                paymentDelegate,
                cardDataMetadataRetriever,
                cardDataMetadataStateRetriever,
                cardInputDataValidator,
                sdkAnalyticsEventLoggingDelegate,
                mockConfigurationDelegate,
            )

        coEvery { paymentDelegate.uiEvent } returns MutableSharedFlow()
    }

    @Disabled
    @Test
    fun `start should initialize and collect uiEvent from paymentDelegate`() {
        // Arrange
        val uiEventFlow = MutableSharedFlow<ComposerUiEvent>()
        coEvery { paymentDelegate.uiEvent } returns uiEventFlow

        val uiEventFlow2 = MutableSharedFlow<ComposerUiEvent>()
        coEvery { component.uiEvent } returns uiEventFlow2

        runTest {
            // When
            component.start(paymentMethodType, PrimerSessionIntent.CHECKOUT)
            advanceUntilIdle()
            uiEventFlow.emit(ComposerUiEvent.Finish)
            advanceUntilIdle()

            // Then
            val events = paymentDelegate.uiEvent.toListDuring(1.0.seconds)
            println(events)
            Assertions.assertTrue(events.any { it is ComposerUiEvent.Finish })
        }
    }

    @Test
    fun `updateCollectedData should emit collectedData and validate`() {
        // Arrange
        val collectedData =
            mockk<PrimerCardData> {
                every { copy(any(), any(), any(), any(), any()) } returns mockk()
                every { cardNetwork } returns CardNetwork.Type.VISA
            }
        val validationErrors = listOf<PrimerInputValidationError>(mockk())
        coEvery { cardInputDataValidator.validate(any()) } returns validationErrors
        coEvery { cardDataMetadataRetriever.retrieveMetadata(any()) } returns mockk<PrimerCardMetadata>()

        coEvery { sdkAnalyticsEventLoggingDelegate.logSdkAnalyticsEvent(any(), any(), any()) } just Runs
        coEvery { cardDataMetadataStateRetriever.handleInputData(any()) } just Runs

        runTest {
            // Act
            component.start(paymentMethodType, PrimerSessionIntent.CHECKOUT)
            component.updateCollectedData(collectedData)
        }

        // Assert
        // Assuming updateCollectedData() causes metadata and validations to be emitted
        coVerify { cardDataMetadataRetriever.retrieveMetadata(collectedData) }
        coVerify { cardInputDataValidator.validate(collectedData) }
    }

    @Test
    fun `handleActivityResultIntent should resume payment on RESULT_OK with ThreeDsInitialLauncherParams`() {
        // Arrange
        val resumeToken = "resumeToken"
        val params =
            mockk<PaymentMethodLauncherParams> {
                every { initialLauncherParams } returns ThreeDsInitialLauncherParams(listOf("2.1.0"))
            }
        val intent =
            mockk<Intent> {
                every { getStringExtra(ThreeDsActivity.RESUME_TOKEN_EXTRA_KEY) } returns resumeToken
            }

        val payment = mockk<Payment>()
        coEvery { paymentDelegate.resumePayment(resumeToken) } returns Result.success(PaymentDecision.Success(payment))

        // Act
        runTest {
            component.handleActivityResultIntent(params, Activity.RESULT_OK, intent)
        }

        // Assert
        coVerify { paymentDelegate.resumePayment(resumeToken) }
    }

    @Test
    fun `handleActivityResultIntent should resume payment on RESULT_OK with ProcessorThreeDsInitialLauncherParams`() {
        // Arrange
        val resumeToken = "resumeToken"
        val params =
            mockk<PaymentMethodLauncherParams> {
                every { initialLauncherParams } returns mockk<ProcessorThreeDsInitialLauncherParams>()
            }
        val intent =
            mockk<Intent> {
                every { getStringExtra(Processor3dsWebViewActivity.RESUME_TOKEN_EXTRA_KEY) } returns resumeToken
            }

        val payment = mockk<Payment>()
        coEvery { paymentDelegate.resumePayment(resumeToken) } returns Result.success(PaymentDecision.Success(payment))

        // Act
        runTest {
            component.handleActivityResultIntent(params, Activity.RESULT_OK, intent)
        }

        // Assert
        coVerify { paymentDelegate.resumePayment(resumeToken) }
    }

    @Test
    fun `handleActivityResultIntent should resume payment on RESULT_OK with null initialLauncherParams`() {
        // Arrange
        val resumeToken = "resumeToken"
        val params =
            mockk<PaymentMethodLauncherParams> {
                every { initialLauncherParams } returns null
            }
        val intent =
            mockk<Intent> {
                every { getStringExtra(ThreeDsActivity.RESUME_TOKEN_EXTRA_KEY) } returns resumeToken
            }

        val payment = mockk<Payment>()
        coEvery { paymentDelegate.resumePayment(resumeToken) } returns Result.success(PaymentDecision.Success(payment))

        // Act
        runTest {
            component.handleActivityResultIntent(params, Activity.RESULT_OK, intent)
        }

        // Assert
        coVerify(exactly = 0) { paymentDelegate.resumePayment(any()) }
        coVerify(exactly = 0) { paymentDelegate.handleError(any()) }
    }

    @Test
    fun `handleActivityResultIntent() should emit PaymentMethodCancelledException when RESULT_CANCELED`() {
        // given
        val params = mockk<PaymentMethodLauncherParams>(relaxed = true)

        coEvery { paymentDelegate.handleError(ofType(PaymentMethodCancelledException::class)) } returns Unit

        // when
        runTest {
            component.start("CARD", PrimerSessionIntent.CHECKOUT)
            component.handleActivityResultIntent(
                params = params,
                resultCode = Activity.RESULT_CANCELED,
                intent = null,
            )
        }

        // then
        coVerify { paymentDelegate.handleError(ofType(PaymentMethodCancelledException::class)) }
    }

    @Test
    fun `handleActivityResultIntent() should emit PaymentMethodCancelledException when RESULT_ERROR`() {
        // given
        val params = mockk<PaymentMethodLauncherParams>(relaxed = true)

        coEvery { paymentDelegate.handleError(any()) } returns Unit

        val exception = Exception("error")

        val intent =
            mockk<Intent> {
                every { getSerializableCompat<Exception>(Processor3dsWebViewActivity.ERROR_KEY) } returns exception
            }

        // when
        runTest {
            component.start("CARD", PrimerSessionIntent.CHECKOUT)
            component.handleActivityResultIntent(
                params = params,
                resultCode = WebViewActivity.RESULT_ERROR,
                intent = intent,
            )
        }

        // then
        coVerify { paymentDelegate.handleError(exception) }
    }

    @Test
    fun `handleActivityStartEvent() should emit Navigate event for ThreeDsInitialLauncherParams when not in mocked flow`() {
        // Given
        every { mockConfigurationDelegate.isMockedFlow() } returns false
        val paymentMethodType = "CARD"
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
            Assertions.assertTrue(
                events.any { it is ComposerUiEvent.Navigate && it.params is CardNative3DSActivityLauncherParams },
            )
            verify { mockConfigurationDelegate.isMockedFlow() }
        }
    }

    @Test
    fun `handleActivityStartEvent() should emit Navigate event for ThreeDsInitialLauncherParams when in mocked flow`() {
        // Given
        every { mockConfigurationDelegate.isMockedFlow() } returns true
        val paymentMethodType = "CARD"
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
            Assertions.assertTrue(
                events.any { it is ComposerUiEvent.Navigate && it.params is MockCard3DSActivityLauncherParams },
            )
            verify { mockConfigurationDelegate.isMockedFlow() }
        }
    }

    @Test
    fun `handleActivityStartEvent() should emit Navigate event for ProcessorThreeDsInitialLauncherParams when not in mocked flow`() {
        // Given
        val paymentMethodType = "CARD"
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
            Assertions.assertTrue(
                events.any { it is ComposerUiEvent.Navigate && it.params is CardProcessor3DSActivityLauncherParams },
            )
            verify(exactly = 0) { mockConfigurationDelegate.isMockedFlow() }
        }
    }

    @Test
    fun `handleActivityStartEvent() should emit Navigate event for ProcessorThreeDsInitialLauncherParams when in mocked flow`() {
        // Given
        val paymentMethodType = "CARD"
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
            Assertions.assertTrue(
                events.any { it is ComposerUiEvent.Navigate && it.params is CardProcessor3DSActivityLauncherParams },
            )
            verify(exactly = 0) { mockConfigurationDelegate.isMockedFlow() }
        }
    }

    @Test
    fun `submit should start tokenization and handle success`() {
        // Arrange
        val primerSessionIntent = PrimerSessionIntent.CHECKOUT
        val cardData =
            mockk<PrimerCardData> {
                every { copy(any(), any(), any(), any(), any()) } returns mockk()
                every { cardNetwork } returns CardNetwork.Type.VISA
            }
        val payment = mockk<Payment>()
        coEvery { cardDataMetadataRetriever.retrieveMetadata(any()) } returns mockk<PrimerCardMetadata>()
        coEvery { cardDataMetadataStateRetriever.handleInputData(any()) } just Runs
        coEvery { sdkAnalyticsEventLoggingDelegate.logSdkAnalyticsEvent(any(), any(), any()) } just Runs
        coEvery { tokenizationDelegate.tokenize(any()) } returns Result.success(mockk())
        coEvery {
            paymentDelegate.handlePaymentMethodToken(
                any(),
                any(),
            )
        } returns Result.success(PaymentDecision.Success(payment))

        runTest {
            component.start(paymentMethodType, primerSessionIntent)
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
        val cardData =
            mockk<PrimerCardData> {
                every { copy(any(), any(), any(), any(), any()) } returns mockk()
                every { cardNetwork } returns CardNetwork.Type.VISA
            }
        val exception = Exception("tokenization failed")
        coEvery { cardDataMetadataRetriever.retrieveMetadata(any()) } returns mockk<PrimerCardMetadata>()
        coEvery { cardDataMetadataStateRetriever.handleInputData(any()) } just Runs
        coEvery { tokenizationDelegate.tokenize(any()) } returns Result.failure(exception)
        coEvery { sdkAnalyticsEventLoggingDelegate.logSdkAnalyticsEvent(any(), any(), any()) } just Runs
        coEvery { paymentDelegate.handlePaymentMethodToken(any(), any()) } returns Result.failure(exception)
        coEvery { paymentDelegate.handleError(exception) } just Runs

        runTest {
            component.start(paymentMethodType, primerSessionIntent)
            component.updateCollectedData(cardData)
            advanceUntilIdle()

            // Act
            component.submit()
        }

        // Assert
        coVerify { tokenizationDelegate.tokenize(any()) }
        coVerify { paymentDelegate.handleError(exception) }
    }
}
