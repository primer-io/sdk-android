package io.primer.android.googlepay.implementation.composer

import android.app.Activity
import android.content.Intent
import com.google.android.gms.common.api.Status
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
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
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.ActionUpdateShippingOptionIdParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.configuration.mock.presentation.MockConfigurationDelegate
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.domain.validation.ValidationRule
import io.primer.android.core.domain.validation.ValidationRulesChain
import io.primer.android.core.extensions.getSerializableCompat
import io.primer.android.core.utils.CoroutineScopeProvider
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.googlepay.InstantExecutorExtension
import io.primer.android.googlepay.implementation.clientSessionActions.presentation.mapper.mapToMultipleActionUpdateParams
import io.primer.android.googlepay.implementation.clientSessionActions.presentation.mapper.mapToShippingOptionIdParams
import io.primer.android.googlepay.implementation.composer.ui.navigation.GooglePayNative3DSActivityLauncherParams
import io.primer.android.googlepay.implementation.composer.ui.navigation.GooglePayProcessor3DSActivityLauncherParams
import io.primer.android.googlepay.implementation.composer.ui.navigation.MockGooglePay3DSActivityLauncherParams
import io.primer.android.googlepay.implementation.errors.domain.exception.GooglePayException
import io.primer.android.googlepay.implementation.payment.delegate.GooglePayPaymentDelegate
import io.primer.android.googlepay.implementation.payment.delegate.ProcessorThreeDsInitialLauncherParams
import io.primer.android.googlepay.implementation.payment.delegate.ThreeDsInitialLauncherParams
import io.primer.android.googlepay.implementation.tokenization.presentation.GooglePayTokenizationCollectorDelegate
import io.primer.android.googlepay.implementation.tokenization.presentation.GooglePayTokenizationDelegate
import io.primer.android.googlepay.implementation.validation.GooglePayShippingMethodUpdateValidator
import io.primer.android.googlepay.implementation.validation.GooglePayValidationRulesResolver
import io.primer.android.googlepay.toListDuring
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.processor3ds.domain.model.Processor3DS
import io.primer.android.processor3ds.ui.Processor3dsWebViewActivity
import io.primer.android.threeds.ui.ThreeDsActivity
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import io.primer.paymentMethodCoreUi.core.ui.webview.WebViewActivity
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

private const val MAPPER_FILE_NAME =
    "io.primer.android.googlepay.implementation.clientSessionActions.presentation.mapper.GooglePayPaymentDataMapperKt"

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class GooglePayComponentTest {
    @RelaxedMockK
    internal lateinit var tokenizationCollectorDelegate: GooglePayTokenizationCollectorDelegate

    @RelaxedMockK
    internal lateinit var tokenizationDelegate: GooglePayTokenizationDelegate

    @RelaxedMockK
    internal lateinit var shippingMethodUpdateValidator: GooglePayShippingMethodUpdateValidator

    @RelaxedMockK
    internal lateinit var actionInteractor: ActionInteractor

    @RelaxedMockK
    internal lateinit var validationRulesResolver: GooglePayValidationRulesResolver

    @RelaxedMockK
    internal lateinit var paymentDelegate: GooglePayPaymentDelegate

    @RelaxedMockK
    internal lateinit var mockConfigurationDelegate: MockConfigurationDelegate

    private lateinit var component: GooglePayComponent

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
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
            GooglePayComponent(
                tokenizationCollectorDelegate = tokenizationCollectorDelegate,
                tokenizationDelegate = tokenizationDelegate,
                shippingMethodUpdateValidator = shippingMethodUpdateValidator,
                actionInteractor = actionInteractor,
                validationRulesResolver = validationRulesResolver,
                paymentDelegate = paymentDelegate,
                mockConfigurationDelegate = mockConfigurationDelegate,
            )

        coEvery { tokenizationCollectorDelegate.uiEvent } returns MutableSharedFlow()
        coEvery { paymentDelegate.uiEvent } returns MutableSharedFlow()

        mockkStatic(MAPPER_FILE_NAME)
        mockkStatic(AutoResolveHelper::class)
        mockkStatic(PaymentData::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(MAPPER_FILE_NAME)
        unmockkStatic(AutoResolveHelper::class)
        unmockkStatic(PaymentData::class)
    }

    @Test
    fun `handleActivityResultIntent() should emit PaymentMethodCancelledException when flow was cancelled`() {
        // given
        val params = mockk<PaymentMethodLauncherParams>(relaxed = true)
        val resultCode = Activity.RESULT_CANCELED

        // when
        runTest {
            component.start("GOOGLE_PAY", PrimerSessionIntent.CHECKOUT)
            component.handleActivityResultIntent(params = params, resultCode = resultCode, intent = null)
        }

        // then
        coVerify { paymentDelegate.handleError(ofType(PaymentMethodCancelledException::class)) }
    }

    @Test
    fun `handleActivityResultIntent() should emit GooglePayException when error result`() {
        // given
        val params = mockk<PaymentMethodLauncherParams>(relaxed = true)
        val resultCode = AutoResolveHelper.RESULT_ERROR
        val mockStatus = mockk<Status>(relaxed = true)

        every { AutoResolveHelper.getStatusFromIntent(any()) } returns mockStatus

        // when
        runTest {
            component.start("GOOGLE_PAY", PrimerSessionIntent.CHECKOUT)
            component.handleActivityResultIntent(params = params, resultCode = resultCode, intent = null)
        }

        // then
        coVerify { paymentDelegate.handleError(ofType(GooglePayException::class)) }
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
            component.start("GOOGLE_PAY", PrimerSessionIntent.CHECKOUT)
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
            component.start("GOOGLE_PAY", PrimerSessionIntent.CHECKOUT)
            component.handleActivityResultIntent(params = params, resultCode = resultCode, intent = intent)
        }

        // Then
        coVerify { paymentDelegate.resumePayment(resumeToken) }
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
            component.start("GOOGLE_PAY", PrimerSessionIntent.CHECKOUT)
            component.handleActivityResultIntent(params = params, resultCode = resultCode, intent = intent)
        }

        // Then
        coVerify { paymentDelegate.resumePayment(resumeToken) }
    }

    @Test
    fun `handleActivityStartEvent() should emit Navigate event for ThreeDsInitialLauncherParams when not in mocked flow`() {
        // Given
        every { mockConfigurationDelegate.isMockedFlow() } returns false
        val threeDsInitialLauncherParams = ThreeDsInitialLauncherParams(listOf("2.0.0", "2.1.0"))
        val params =
            PaymentMethodLauncherParams(
                paymentMethodType = "GOOGLE_PAY",
                sessionIntent = PrimerSessionIntent.CHECKOUT,
                initialLauncherParams = threeDsInitialLauncherParams,
            )

        runTest {
            // When
            component.start("GOOGLE_PAY", PrimerSessionIntent.CHECKOUT)
            component.handleActivityStartEvent(params)

            // Then
            val events = component.uiEvent.toListDuring(1.0.seconds)
            assertTrue(
                events.any { it is ComposerUiEvent.Navigate && it.params is GooglePayNative3DSActivityLauncherParams },
            )
            verify { mockConfigurationDelegate.isMockedFlow() }
        }
    }

    @Test
    fun `handleActivityStartEvent() should emit Navigate event for ThreeDsInitialLauncherParams when in mocked flow`() {
        // Given
        every { mockConfigurationDelegate.isMockedFlow() } returns true
        val threeDsInitialLauncherParams = ThreeDsInitialLauncherParams(listOf("2.0.0", "2.1.0"))
        val params =
            PaymentMethodLauncherParams(
                paymentMethodType = "GOOGLE_PAY",
                sessionIntent = PrimerSessionIntent.CHECKOUT,
                initialLauncherParams = threeDsInitialLauncherParams,
            )

        runTest {
            // When
            component.start("GOOGLE_PAY", PrimerSessionIntent.CHECKOUT)
            component.handleActivityStartEvent(params)

            // Then
            val events = component.uiEvent.toListDuring(1.0.seconds)
            assertTrue(
                events.any {
                    it is ComposerUiEvent.Navigate && it.params is MockGooglePay3DSActivityLauncherParams
                },
            )
            verify { mockConfigurationDelegate.isMockedFlow() }
        }
    }

    @Test
    fun `handleActivityStartEvent() should emit Navigate event for ProcessorThreeDsInitialLauncherParams when is not mocked flow`() {
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
                paymentMethodType = "GOOGLE_PAY",
                sessionIntent = PrimerSessionIntent.CHECKOUT,
                initialLauncherParams = threeDsInitialLauncherParams,
            )

        runTest {
            // When
            component.start("GOOGLE_PAY", PrimerSessionIntent.CHECKOUT)
            component.handleActivityStartEvent(params)

            // Then
            val events = component.uiEvent.toListDuring(1.0.seconds)
            assertTrue(
                events.any {
                    it is ComposerUiEvent.Navigate && it.params is GooglePayProcessor3DSActivityLauncherParams
                },
            )
            verify(exactly = 0) { mockConfigurationDelegate.isMockedFlow() }
        }
    }

    @Test
    fun `handleActivityStartEvent() should emit Navigate event for ProcessorThreeDsInitialLauncherParams when is mocked flow`() {
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
                paymentMethodType = "GOOGLE_PAY",
                sessionIntent = PrimerSessionIntent.CHECKOUT,
                initialLauncherParams = threeDsInitialLauncherParams,
            )

        runTest {
            // When
            component.start("GOOGLE_PAY", PrimerSessionIntent.CHECKOUT)
            component.handleActivityStartEvent(params)

            // Then
            val events = component.uiEvent.toListDuring(1.0.seconds)
            assertTrue(
                events.any {
                    it is ComposerUiEvent.Navigate && it.params is GooglePayProcessor3DSActivityLauncherParams
                },
            )
            verify(exactly = 0) { mockConfigurationDelegate.isMockedFlow() }
        }
    }

    @Test
    fun `handleActivityStartEvent() should start data collection when initialLauncherParams is null`() {
        // Given
        val params =
            PaymentMethodLauncherParams(
                paymentMethodType = "GOOGLE_PAY",
                sessionIntent = PrimerSessionIntent.CHECKOUT,
                initialLauncherParams = null,
            )

        runTest {
            // When
            component.start("GOOGLE_PAY", PrimerSessionIntent.CHECKOUT)
            component.handleActivityStartEvent(params)
        }

        // Then
        coVerify { tokenizationCollectorDelegate.startDataCollection(any()) }
    }

    @Test
    fun `handlePaymentData() should end up calling tokenize() and handlePaymentMethodToken() when the validation returns Success`() {
        // Given
        val updateParams = mockk<MultipleActionUpdateParams>()
        every { any<PaymentData>().mapToMultipleActionUpdateParams() } returns updateParams
        val idParams = mockk<ActionUpdateShippingOptionIdParams>()
        every { any<PaymentData>().mapToShippingOptionIdParams() } returns idParams
        val paymentData = mockk<PaymentData>(relaxed = true)

        every { PaymentData.getFromIntent(any()) } returns paymentData

        val validationRule =
            mockk<ValidationRule<PaymentData?>>(relaxed = true) {
                every { validate(any()) } returns ValidationResult.Success
            }

        val validationRulesChain =
            mockk<ValidationRulesChain<PaymentData?>>(relaxed = true) {
                every { rules } returns listOf(validationRule)
            }

        every { validationRulesResolver.resolve() } returns validationRulesChain

        val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>(relaxed = true)

        coEvery { actionInteractor(any()) } returns Result.success(mockk())
        coEvery { shippingMethodUpdateValidator(idParams) } returns Result.success(Unit)
        coEvery { tokenizationDelegate.tokenize(any()) } returns Result.success(paymentMethodTokenData)

        // When
        runTest {
            component.start("GOOGLE_PAY", PrimerSessionIntent.CHECKOUT)

            component.handleActivityResultIntent(
                params =
                    PaymentMethodLauncherParams(
                        paymentMethodType = "GOOGLE_PAY",
                        sessionIntent = PrimerSessionIntent.CHECKOUT,
                    ),
                resultCode = Activity.RESULT_OK,
                intent = Intent(),
            )
        }

        // Then
        coVerify {
            validationRulesResolver.resolve()
            actionInteractor(updateParams)
            actionInteractor(MultipleActionUpdateParams(listOf(idParams)))
            shippingMethodUpdateValidator(idParams)
            tokenizationDelegate.tokenize(any())
            paymentDelegate.handlePaymentMethodToken(any(), any())
        }
    }

    @Test
    fun `handlePaymentData() should end up calling handleError() when tokenize() returns a failure result`() {
        // Given
        val updateParams = mockk<MultipleActionUpdateParams>()
        every { any<PaymentData>().mapToMultipleActionUpdateParams() } returns updateParams
        val idParams = mockk<ActionUpdateShippingOptionIdParams>()
        every { any<PaymentData>().mapToShippingOptionIdParams() } returns idParams
        val paymentData = mockk<PaymentData>(relaxed = true)

        every { PaymentData.getFromIntent(any()) } returns paymentData

        val validationRule =
            mockk<ValidationRule<PaymentData?>>(relaxed = true) {
                every { validate(any()) } returns ValidationResult.Success
            }

        val validationRulesChain =
            mockk<ValidationRulesChain<PaymentData?>>(relaxed = true) {
                every { rules } returns listOf(validationRule)
            }

        every { validationRulesResolver.resolve() } returns validationRulesChain

        coEvery { actionInteractor(any()) } returns Result.success(mockk())
        coEvery { shippingMethodUpdateValidator(idParams) } returns Result.success(Unit)
        val error = Throwable()
        coEvery { tokenizationDelegate.tokenize(any()) } returns Result.failure(error)

        // When
        runTest {
            component.start("GOOGLE_PAY", PrimerSessionIntent.CHECKOUT)

            component.handleActivityResultIntent(
                params =
                    PaymentMethodLauncherParams(
                        paymentMethodType = "GOOGLE_PAY",
                        sessionIntent = PrimerSessionIntent.CHECKOUT,
                    ),
                resultCode = Activity.RESULT_OK,
                intent = Intent(),
            )
        }

        // Then
        coVerify {
            validationRulesResolver.resolve()
            actionInteractor(updateParams)
            actionInteractor(MultipleActionUpdateParams(listOf(idParams)))
            shippingMethodUpdateValidator(idParams)
            tokenizationDelegate.tokenize(any())
            paymentDelegate.handleError(error)
        }
        coVerify(exactly = 0) {
            paymentDelegate.handlePaymentMethodToken(any(), any())
        }
    }

    @Test
    fun `handlePaymentData() should end up calling handleError() when validation fails`() {
        // Given
        val updateParams = mockk<MultipleActionUpdateParams>()
        every { any<PaymentData>().mapToMultipleActionUpdateParams() } returns updateParams
        val idParams = mockk<ActionUpdateShippingOptionIdParams>()
        every { any<PaymentData>().mapToShippingOptionIdParams() } returns idParams
        val paymentData = mockk<PaymentData>(relaxed = true)

        every { PaymentData.getFromIntent(any()) } returns paymentData

        val validationRule =
            mockk<ValidationRule<PaymentData?>>(relaxed = true) {
                every { validate(any()) } returns ValidationResult.Failure(Exception("Validation failed"))
            }

        val validationRulesChain =
            mockk<ValidationRulesChain<PaymentData?>>(relaxed = true) {
                every { rules } returns listOf(validationRule)
            }

        every { validationRulesResolver.resolve() } returns validationRulesChain

        // When
        runTest {
            component.start("GOOGLE_PAY", PrimerSessionIntent.CHECKOUT)

            component.handleActivityResultIntent(
                params =
                    PaymentMethodLauncherParams(
                        paymentMethodType = "GOOGLE_PAY",
                        sessionIntent = PrimerSessionIntent.CHECKOUT,
                    ),
                resultCode = Activity.RESULT_OK,
                intent = Intent(),
            )
        }

        // Then
        coVerify {
            validationRulesResolver.resolve()
            paymentDelegate.handleError(any())
        }
        coVerify(exactly = 0) {
            actionInteractor(any())
            shippingMethodUpdateValidator(any())
            tokenizationDelegate.tokenize(any())
            paymentDelegate.handlePaymentMethodToken(any(), any())
        }
    }

    @Test
    fun `handlePaymentData() should end up calling handleError() with PaymentMethodCancelledException when the tokenization fails with CancellationException`() {
        // Given
        val updateParams = mockk<MultipleActionUpdateParams>()
        every { any<PaymentData>().mapToMultipleActionUpdateParams() } returns updateParams
        val idParams = mockk<ActionUpdateShippingOptionIdParams>()
        every { any<PaymentData>().mapToShippingOptionIdParams() } returns idParams
        val paymentData = mockk<PaymentData>(relaxed = true)

        every { PaymentData.getFromIntent(any()) } returns paymentData

        val validationRule =
            mockk<ValidationRule<PaymentData?>>(relaxed = true) {
                every { validate(any()) } returns ValidationResult.Success
            }

        val validationRulesChain =
            mockk<ValidationRulesChain<PaymentData?>>(relaxed = true) {
                every { rules } returns listOf(validationRule)
            }

        every { validationRulesResolver.resolve() } returns validationRulesChain

        coEvery { actionInteractor(any()) } returns Result.success(mockk())
        coEvery { shippingMethodUpdateValidator(idParams) } returns Result.success(Unit)
        coEvery { tokenizationDelegate.tokenize(any()) } throws CancellationException()

        // When
        runTest {
            component.start("GOOGLE_PAY", PrimerSessionIntent.CHECKOUT)

            component.handleActivityResultIntent(
                params =
                    PaymentMethodLauncherParams(
                        paymentMethodType = "GOOGLE_PAY",
                        sessionIntent = PrimerSessionIntent.CHECKOUT,
                    ),
                resultCode = Activity.RESULT_OK,
                intent = Intent(),
            )
        }

        // Then
        coVerify {
            validationRulesResolver.resolve()
            actionInteractor(updateParams)
            actionInteractor(MultipleActionUpdateParams(listOf(idParams)))
            shippingMethodUpdateValidator(idParams)
            tokenizationDelegate.tokenize(any())
            paymentDelegate.handleError(PaymentMethodCancelledException(paymentMethodType = "GOOGLE_PAY"))
        }

        coVerify(exactly = 0) {
            paymentDelegate.handlePaymentMethodToken(any(), any())
        }
    }
}
