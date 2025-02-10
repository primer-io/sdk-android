package io.primer.android.klarna.api.composable

import android.content.Context
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentView
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentViewCallback
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentsSDKError
import io.mockk.Awaits
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.configuration.mock.presentation.MockConfigurationDelegate
import io.primer.android.core.extensions.collectIn
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.klarna.api.component.KlarnaComponent
import io.primer.android.klarna.api.component.MOCK_EMISSION_DELAY
import io.primer.android.klarna.implementation.analytics.KlarnaPaymentAnalyticsConstants
import io.primer.android.klarna.implementation.errors.data.exception.KlarnaSdkErrorException
import io.primer.android.klarna.implementation.errors.data.exception.KlarnaUserUnapprovedException
import io.primer.android.klarna.implementation.payment.presentation.KlarnaPaymentDelegate
import io.primer.android.klarna.implementation.session.data.validation.validator.KlarnaPaymentCategoryValidator
import io.primer.android.klarna.implementation.session.data.validation.validator.KlarnaPaymentFinalizationValidator
import io.primer.android.klarna.implementation.session.domain.models.KlarnaPaymentCategory
import io.primer.android.klarna.implementation.session.domain.models.KlarnaSession
import io.primer.android.klarna.implementation.session.presentation.GetKlarnaAuthorizationSessionDataDelegate
import io.primer.android.klarna.implementation.session.presentation.KlarnaSessionCreationDelegate
import io.primer.android.klarna.implementation.tokenization.presentation.KlarnaTokenizationDelegate
import io.primer.android.klarna.implementation.tokenization.presentation.KlarnaTokenizationInputable
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsValidationErrorLoggingDelegate
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
class KlarnaComponentTest {
    private val returnIntentUrl = "scheme://host"

    private val authorizationSessionData = """{"a":"b"}"""

    @MockK
    private lateinit var primerSessionIntent: PrimerSessionIntent

    @MockK
    private lateinit var createKlarnaPaymentView: (Context, String, KlarnaPaymentViewCallback, String) ->
    KlarnaPaymentView

    @MockK
    private lateinit var klarnaTokenizationDelegate: KlarnaTokenizationDelegate

    @MockK
    private lateinit var klarnaPaymentDelegate: KlarnaPaymentDelegate

    @MockK
    private lateinit var klarnaSessionCreationDelegate: KlarnaSessionCreationDelegate

    @MockK
    private lateinit var mockConfigurationDelegate: MockConfigurationDelegate

    @MockK
    private lateinit var eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate

    @MockK
    private lateinit var validationErrorLoggingDelegate: SdkAnalyticsValidationErrorLoggingDelegate

    @MockK
    private lateinit var errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate

    @MockK
    private lateinit var authorizationSessionDataDelegate: GetKlarnaAuthorizationSessionDataDelegate

    @MockK
    private lateinit var errorMapperRegistry: ErrorMapperRegistry

    @MockK
    private lateinit var primerSettings: PrimerSettings

    private lateinit var component: KlarnaComponent

    @BeforeEach
    fun setUp() {
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "uuid"

        every {
            authorizationSessionDataDelegate.getAuthorizationSessionDataOrNull()
        } returns authorizationSessionData
        confirmVerified(mockConfigurationDelegate)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(UUID::class)

        confirmVerified(
            klarnaTokenizationDelegate,
            klarnaSessionCreationDelegate,
            errorMapperRegistry,
        )
    }

    @Test
    fun `start() should log event and emit session creation step when session creation delegate succeeds`() =
        runTest {
            initComponent(isMockedFlow = false)
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            val availableCategories = mockk<List<KlarnaPaymentCategory>>()
            val klarnaSession =
                mockk<KlarnaSession> {
                    every { this@mockk.availableCategories } returns availableCategories
                }
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            coEvery {
                klarnaSessionCreationDelegate.createSession(primerSessionIntent)
            } returns Result.success(klarnaSession)
            val errors = mutableListOf<PrimerError>()
            val errorJob = component.componentError.collectIn(errors, this)
            val validationStatuses =
                mutableListOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>()
            val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
            val steps = mutableListOf<KlarnaPaymentStep>()
            val stepJob = component.componentStep.collectIn(steps, this)

            component.start()
            delay(1.seconds)
            errorJob.cancel()
            validationJob.cancel()
            stepJob.cancel()

            coVerify(exactly = 0) {
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerError>())
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerValidationError>())
            }
            coVerify(exactly = 1) {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_START_METHOD,
                    paymentMethodType = PaymentMethodType.KLARNA.name,
                )
                klarnaSessionCreationDelegate.createSession(primerSessionIntent)
            }
            assertEquals(emptyList<PrimerError>(), errors)
            assertEquals(
                emptyList<PrimerValidationStatus<KlarnaPaymentCollectableData>>(),
                validationStatuses,
            )
            assertEquals(listOf(KlarnaPaymentStep.PaymentSessionCreated(availableCategories)), steps)
        }

    @Test
    fun `start() should log event, emit error and call error handler when session creation delegate fails and in HEADLESS mode`() =
        runTest {
            initComponent(isMockedFlow = false)
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Awaits
            val exception = Exception()
            coEvery {
                klarnaSessionCreationDelegate.createSession(primerSessionIntent)
            } returns Result.failure(exception)
            val primerError = mockk<PrimerError>()
            coEvery { errorMapperRegistry.getPrimerError(any()) } returns primerError
            coEvery { klarnaPaymentDelegate.handleError(any()) } just Runs
            val errors = mutableListOf<PrimerError>()
            val errorJob = component.componentError.collectIn(errors, this)
            val validationStatuses =
                mutableListOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>()
            val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
            val steps = mutableListOf<KlarnaPaymentStep>()
            val stepJob = component.componentStep.collectIn(steps, this)

            component.start()
            delay(1.seconds)
            errorJob.cancel()
            validationJob.cancel()
            stepJob.cancel()

            verify(exactly = 1) {
                errorMapperRegistry.getPrimerError(
                    withArg { it is KlarnaComponent.CheckoutFailureException && it.cause == exception },
                )
            }
            coVerify(exactly = 0) {
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerError>())
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerValidationError>())
            }
            coVerify(exactly = 1) {
                klarnaPaymentDelegate.handleError(
                    withArg { it is KlarnaComponent.CheckoutFailureException && it.cause == exception },
                )
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_START_METHOD,
                    paymentMethodType = PaymentMethodType.KLARNA.name,
                )
                klarnaSessionCreationDelegate.createSession(primerSessionIntent)
                errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
            }
            assertEquals(listOf(primerError), errors)
            assertEquals(
                emptyList<PrimerValidationStatus<KlarnaPaymentCollectableData>>(),
                validationStatuses,
            )
            assertEquals(emptyList<KlarnaPaymentStep>(), steps)
        }

    @Test
    fun `start() should log event, log, emit error, and call error handler when session creation delegate fails and in DROP-in mode`() =
        runTest {
            initComponent(isMockedFlow = false)
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.DROP_IN
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Awaits
            val exception = Exception()
            coEvery {
                klarnaSessionCreationDelegate.createSession(primerSessionIntent)
            } returns Result.failure(exception)
            val primerError = mockk<PrimerError>()
            coEvery { errorMapperRegistry.getPrimerError(any()) } returns primerError
            coEvery {
                klarnaPaymentDelegate.handleError(any())
            } just Runs

            val errors = mutableListOf<PrimerError>()
            val errorJob = component.componentError.collectIn(errors, this)
            val validationStatuses =
                mutableListOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>()
            val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
            val steps = mutableListOf<KlarnaPaymentStep>()
            val stepJob = component.componentStep.collectIn(steps, this)

            component.start()
            delay(1.seconds)
            errorJob.cancel()
            validationJob.cancel()
            stepJob.cancel()

            coVerify(exactly = 1) {
                errorMapperRegistry.getPrimerError(
                    withArg { it is KlarnaComponent.CheckoutFailureException && it.cause == exception },
                )
                klarnaPaymentDelegate.handleError(
                    withArg { it is KlarnaComponent.CheckoutFailureException && it.cause == exception },
                )
            }
            coVerify(exactly = 0) {
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerError>())
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerValidationError>())
            }
            coVerify(exactly = 1) {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_START_METHOD,
                    paymentMethodType = PaymentMethodType.KLARNA.name,
                )
                klarnaSessionCreationDelegate.createSession(primerSessionIntent)
                errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
            }
            assertEquals(listOf(primerError), errors)
            assertEquals(
                emptyList<PrimerValidationStatus<KlarnaPaymentCollectableData>>(),
                validationStatuses,
            )
            assertEquals(emptyList<KlarnaPaymentStep>(), steps)
        }

    @Test
    fun `updateCollectedData() should log event and emit payment view loaded step and valid status when KlarnaPaymentCategoryValidator validate() returns null`() =
        runTest {
            initComponent(isMockedFlow = false)
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            mockkObject(KlarnaPaymentCategoryValidator)
            every { KlarnaPaymentCategoryValidator.validate(any(), any()) } returns null
            val klarnaPaymentView = mockk<KlarnaPaymentView>(relaxed = true)
            val klarnaPaymentViewCallback = slot<KlarnaPaymentViewCallback>()
            every {
                createKlarnaPaymentView.invoke(
                    any(),
                    any(),
                    capture(klarnaPaymentViewCallback),
                    any(),
                )
            } returns klarnaPaymentView
            val paymentCategory =
                mockk<KlarnaPaymentCategory> {
                    every { identifier } returns "identifier"
                }
            val klarnaSession =
                mockk<KlarnaSession> {
                    every { clientToken } returns "clientToken"
                    every { availableCategories } returns listOf(paymentCategory)
                }
            component.klarnaSession = klarnaSession
            val collectableData =
                KlarnaPaymentCollectableData.PaymentOptions(mockk(), returnIntentUrl, paymentCategory)
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            val errors = mutableListOf<PrimerError>()
            val errorJob = component.componentError.collectIn(errors, this)
            val validationStatuses =
                mutableListOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>()
            val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
            val steps = mutableListOf<KlarnaPaymentStep>()
            val stepJob = component.componentStep.collectIn(steps, this)

            component.updateCollectedData(collectableData)
            delay(1.seconds)
            with(klarnaPaymentViewCallback.captured) {
                onInitialized(klarnaPaymentView)
                onLoaded(klarnaPaymentView)
            }
            delay(1.seconds)
            errorJob.cancel()
            validationJob.cancel()
            stepJob.cancel()

            coVerify(exactly = 1) {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_COLLECTED_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.KLARNA.name,
                )
            }
            coVerify(exactly = 0) {
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerError>())
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerValidationError>())
            }
            verify(exactly = 1) {
                KlarnaPaymentCategoryValidator.validate(listOf(paymentCategory), paymentCategory)
                klarnaPaymentView.initialize("clientToken", returnIntentUrl)
                klarnaPaymentView.load(null)
            }
            assertEquals(emptyList<PrimerError>(), errors)
            assertEquals(
                listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                    PrimerValidationStatus.Validating(collectableData),
                    PrimerValidationStatus.Valid(collectableData),
                ),
                validationStatuses,
            )
            assertInstanceOf(KlarnaPaymentStep.PaymentViewLoaded::class.java, steps.last())
            unmockkObject(KlarnaPaymentCategoryValidator)
        }

    @Test
    fun `updateCollectedData() should log event and emit valid status when KlarnaPaymentFinalizationValidator validate() returns null`() =
        runTest {
            initComponent(isMockedFlow = false)
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            mockkObject(KlarnaPaymentFinalizationValidator)
            every { KlarnaPaymentFinalizationValidator.validate(any()) } returns null
            component.isFinalizationRequired = true
            val paymentCategory =
                mockk<KlarnaPaymentCategory> {
                    every { identifier } returns "identifier"
                }
            val klarnaSession =
                mockk<KlarnaSession> {
                    every { clientToken } returns "clientToken"
                    every { availableCategories } returns listOf(paymentCategory)
                }
            component.klarnaSession = klarnaSession
            val collectableData = KlarnaPaymentCollectableData.FinalizePayment
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            val errors = mutableListOf<PrimerError>()
            val errorJob = component.componentError.collectIn(errors, this)
            val validationStatuses =
                mutableListOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>()
            val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
            val steps = mutableListOf<KlarnaPaymentStep>()
            val stepJob = component.componentStep.collectIn(steps, this)

            component.updateCollectedData(collectableData)
            delay(1.seconds)
            errorJob.cancel()
            validationJob.cancel()
            stepJob.cancel()

            coVerify(exactly = 1) {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_COLLECTED_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.KLARNA.name,
                )
            }
            coVerify(exactly = 0) {
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerError>())
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerValidationError>())
            }
            verify(exactly = 1) {
                KlarnaPaymentFinalizationValidator.validate(isFinalizationRequired = true)
            }
            assertEquals(emptyList<PrimerError>(), errors)
            assertEquals(
                listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                    PrimerValidationStatus.Validating(collectableData),
                    PrimerValidationStatus.Valid(collectableData),
                ),
                validationStatuses,
            )
            assertEquals(emptyList<KlarnaPaymentStep>(), steps)
            unmockkObject(KlarnaPaymentFinalizationValidator)
        }

    @Test
    fun `updateCollectedData() should log event, validation errors and emit invalid status when KlarnaPaymentFinalizationValidator validate() returns validation error`() =
        runTest {
            initComponent(isMockedFlow = false)
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            coEvery { validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerValidationError>()) } just Runs
            mockkObject(KlarnaPaymentFinalizationValidator)
            component.isFinalizationRequired = false
            val validationError = mockk<PrimerValidationError>()
            every { KlarnaPaymentFinalizationValidator.validate(any()) } returns validationError
            val collectableData = KlarnaPaymentCollectableData.FinalizePayment
            val errors = mutableListOf<PrimerError>()
            val errorJob = component.componentError.collectIn(errors, this)
            val validationStatuses =
                mutableListOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>()
            val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
            val steps = mutableListOf<KlarnaPaymentStep>()
            val stepJob = component.componentStep.collectIn(steps, this)

            component.updateCollectedData(collectableData)
            delay(1.seconds)
            errorJob.cancel()
            validationJob.cancel()
            stepJob.cancel()

            coVerify(exactly = 1) {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_COLLECTED_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.KLARNA.name,
                )
            }
            coVerify(exactly = 1) {
                validationErrorLoggingDelegate.logSdkAnalyticsError(validationError)
            }
            assertEquals(emptyList<PrimerError>(), errors)
            assertEquals(
                listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                    PrimerValidationStatus.Validating(collectableData),
                    PrimerValidationStatus.Invalid(listOf(validationError), collectableData),
                ),
                validationStatuses,
            )
            assertEquals(emptyList<KlarnaPaymentStep>(), steps)
            unmockkStatic(KlarnaPaymentFinalizationValidator::class)
        }

    @Test
    fun `updateCollectedData() should log event, validation errors and emit invalid status when KlarnaPaymentCategoryValidator validate() returns validation error`() =
        runTest {
            initComponent(isMockedFlow = false)
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            coEvery { validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerValidationError>()) } just Runs
            mockkObject(KlarnaPaymentCategoryValidator)
            val validationError = mockk<PrimerValidationError>()
            every { KlarnaPaymentCategoryValidator.validate(any(), any()) } returns validationError
            val paymentCategory = mockk<KlarnaPaymentCategory>()
            val collectableData =
                KlarnaPaymentCollectableData.PaymentOptions(mockk(), returnIntentUrl, paymentCategory)
            val errors = mutableListOf<PrimerError>()
            val errorJob = component.componentError.collectIn(errors, this)
            val validationStatuses =
                mutableListOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>()
            val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
            val steps = mutableListOf<KlarnaPaymentStep>()
            val stepJob = component.componentStep.collectIn(steps, this)

            component.updateCollectedData(collectableData)
            delay(1.seconds)
            errorJob.cancel()
            validationJob.cancel()
            stepJob.cancel()

            coVerify(exactly = 1) {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_COLLECTED_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.KLARNA.name,
                )
            }
            coVerify(exactly = 1) {
                validationErrorLoggingDelegate.logSdkAnalyticsError(validationError)
            }
            coVerify(exactly = 0) {
                createKlarnaPaymentView.invoke(any(), any(), any(), any())
            }
            assertEquals(emptyList<PrimerError>(), errors)
            assertEquals(
                listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                    PrimerValidationStatus.Validating(collectableData),
                    PrimerValidationStatus.Invalid(listOf(validationError), collectableData),
                ),
                validationStatuses,
            )
            assertEquals(emptyList<KlarnaPaymentStep>(), steps)
            unmockkStatic(KlarnaPaymentCategoryValidator::class)
        }

    @Test
    fun `submit() should start authorization without auto finalization, log event, tokenize, handle payment token and emit authorized step when payment is approved and integration type is HEADLESS`() =
        runTest {
            initComponent(isMockedFlow = false)
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            mockkObject(KlarnaPaymentCategoryValidator)
            every { KlarnaPaymentCategoryValidator.validate(any(), any()) } returns null
            val klarnaPaymentViewCallback = slot<KlarnaPaymentViewCallback>()
            val klarnaPaymentView = mockk<KlarnaPaymentView>(relaxed = true)
            every {
                createKlarnaPaymentView.invoke(
                    any(),
                    any(),
                    capture(klarnaPaymentViewCallback),
                    any(),
                )
            } returns klarnaPaymentView

            val paymentMethodData = mockk<PrimerPaymentMethodTokenData>(relaxed = true)
            coEvery {
                klarnaTokenizationDelegate.tokenize(any())
            } returns Result.success(paymentMethodData)

            val paymentDecision = mockk<PaymentDecision>(relaxed = true)
            coEvery {
                klarnaPaymentDelegate.handlePaymentMethodToken(any(), any())
            } returns Result.success(paymentDecision)

            val paymentCategory =
                mockk<KlarnaPaymentCategory> {
                    every { identifier } returns "identifier"
                }
            val klarnaSession =
                mockk<KlarnaSession> {
                    every { sessionId } returns "sessionId"
                    every { clientToken } returns "clientToken"
                    every { availableCategories } returns listOf(paymentCategory)
                }
            component.klarnaSession = klarnaSession
            val errors = mutableListOf<PrimerError>()
            val errorJob = component.componentError.collectIn(errors, this)
            val validationStatuses =
                mutableListOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>()
            val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
            val steps = mutableListOf<KlarnaPaymentStep>()
            val stepJob = component.componentStep.collectIn(steps, this)
            val collectableData =
                KlarnaPaymentCollectableData.PaymentOptions(
                    context = mockk(),
                    returnIntentUrl = returnIntentUrl,
                    paymentCategory = paymentCategory,
                )

            component.updateCollectedData(collectableData)
            delay(1.seconds)
            component.submit()
            delay(1.seconds)
            klarnaPaymentViewCallback.captured.onAuthorized(
                klarnaPaymentView,
                true,
                "authToken",
                false,
            )
            delay(1.seconds)
            errorJob.cancel()
            validationJob.cancel()
            stepJob.cancel()

            verify(exactly = 1) {
                klarnaPaymentView.authorize(false, authorizationSessionData)
                KlarnaPaymentCategoryValidator.validate(listOf(paymentCategory), paymentCategory)
            }
            coVerify(exactly = 1) {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.KLARNA.name,
                )
                klarnaTokenizationDelegate.tokenize(any())
            }
            coVerify(exactly = 0) {
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerError>())
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerValidationError>())
            }
            assertEquals(component.isFinalizationRequired, false)
            assertEquals(emptyList<PrimerError>(), errors)
            assertEquals(
                listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                    PrimerValidationStatus.Validating(collectableData),
                    PrimerValidationStatus.Valid(collectableData),
                ),
                validationStatuses,
            )
            assertEquals(
                listOf(
                    KlarnaPaymentStep.PaymentSessionAuthorized(true),
                ),
                steps.takeLast(2),
            )
            unmockkObject(KlarnaPaymentCategoryValidator)
        }

    @Test
    fun `submit() should log event, tokenize, handle token data and emit authorized step when in mocked flow and integration type is HEADLESS`() =
        runTest {
            initComponent(isMockedFlow = true)
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            mockkObject(KlarnaPaymentCategoryValidator)
            every { KlarnaPaymentCategoryValidator.validate(any(), any()) } returns null
            val klarnaPaymentViewCallback = slot<KlarnaPaymentViewCallback>()
            val klarnaPaymentView = mockk<KlarnaPaymentView>(relaxed = true)
            every {
                createKlarnaPaymentView.invoke(
                    any(),
                    any(),
                    capture(klarnaPaymentViewCallback),
                    any(),
                )
            } returns klarnaPaymentView
            val paymentMethodData = mockk<PrimerPaymentMethodTokenData>(relaxed = true)
            coEvery {
                klarnaTokenizationDelegate.tokenize(any())
            } returns Result.success(paymentMethodData)
            val paymentDecision = mockk<PaymentDecision>(relaxed = true)
            coEvery {
                klarnaPaymentDelegate.handlePaymentMethodToken(any(), any())
            } returns Result.success(paymentDecision)
            val paymentCategory =
                mockk<KlarnaPaymentCategory> {
                    every { identifier } returns "identifier"
                }
            val klarnaSession =
                mockk<KlarnaSession> {
                    every { sessionId } returns "sessionId"
                    every { clientToken } returns "clientToken"
                    every { availableCategories } returns listOf(paymentCategory)
                }
            component.klarnaSession = klarnaSession
            val errors = mutableListOf<PrimerError>()
            val errorJob = component.componentError.collectIn(errors, this)
            val validationStatuses =
                mutableListOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>()
            val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
            val steps = mutableListOf<KlarnaPaymentStep>()
            val stepJob = component.componentStep.collectIn(steps, this)
            val collectableData =
                KlarnaPaymentCollectableData.PaymentOptions(
                    context =
                    mockk {
                        every { getText(any()) } returns "text"
                    },
                    returnIntentUrl = returnIntentUrl,
                    paymentCategory = paymentCategory,
                )

            component.updateCollectedData(collectableData)
            delay(2.seconds)
            component.submit()
            delay(MOCK_EMISSION_DELAY)
            advanceUntilIdle()
            errorJob.cancel()
            validationJob.cancel()
            stepJob.cancel()

            verify(exactly = 1) {
                KlarnaPaymentCategoryValidator.validate(listOf(paymentCategory), paymentCategory)
            }
            verify(exactly = 0) {
                klarnaPaymentView.authorize(any(), any())
            }
            coVerify(exactly = 1) {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.KLARNA.name,
                )
                klarnaTokenizationDelegate.tokenize(any())
            }
            coVerify(exactly = 0) {
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerError>())
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerValidationError>())
            }
            assertEquals(component.isFinalizationRequired, false)
            assertEquals(emptyList<PrimerError>(), errors)
            assertEquals(
                listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                    PrimerValidationStatus.Validating(collectableData),
                    PrimerValidationStatus.Valid(collectableData),
                ),
                validationStatuses,
            )
            assertInstanceOf(KlarnaPaymentStep.PaymentViewLoaded::class.java, steps.first())
            assertEquals(
                KlarnaPaymentStep.PaymentSessionAuthorized(true),
                steps.last(),
            )
            unmockkObject(KlarnaPaymentCategoryValidator)
        }

    @Test
    fun `submit() should start authorization with auto finalization, log event, tokenize, handle token data and emit authorized step when payment is approved when integration type is DROP-IN`() =
        runTest {
            initComponent(isMockedFlow = false)
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.DROP_IN
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            mockkObject(KlarnaPaymentCategoryValidator)
            every { KlarnaPaymentCategoryValidator.validate(any(), any()) } returns null
            val klarnaPaymentViewCallback = slot<KlarnaPaymentViewCallback>()
            val klarnaPaymentView = mockk<KlarnaPaymentView>(relaxed = true)
            every {
                createKlarnaPaymentView.invoke(
                    any(),
                    any(),
                    capture(klarnaPaymentViewCallback),
                    any(),
                )
            } returns klarnaPaymentView

            val paymentMethodData = mockk<PrimerPaymentMethodTokenData>(relaxed = true)
            coEvery {
                klarnaTokenizationDelegate.tokenize(any())
            } returns Result.success(paymentMethodData)

            val paymentDecision = mockk<PaymentDecision>(relaxed = true)
            coEvery {
                klarnaPaymentDelegate.handlePaymentMethodToken(any(), any())
            } returns Result.success(paymentDecision)

            val paymentCategory =
                mockk<KlarnaPaymentCategory> {
                    every { identifier } returns "identifier"
                }
            val klarnaSession =
                mockk<KlarnaSession> {
                    every { sessionId } returns "sessionId"
                    every { clientToken } returns "clientToken"
                    every { availableCategories } returns listOf(paymentCategory)
                }
            component.klarnaSession = klarnaSession
            val errors = mutableListOf<PrimerError>()
            val errorJob = component.componentError.collectIn(errors, this)
            val validationStatuses =
                mutableListOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>()
            val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
            val steps = mutableListOf<KlarnaPaymentStep>()
            val stepJob = component.componentStep.collectIn(steps, this)
            val collectableData =
                KlarnaPaymentCollectableData.PaymentOptions(
                    context = mockk(),
                    returnIntentUrl = returnIntentUrl,
                    paymentCategory = paymentCategory,
                )

            component.updateCollectedData(collectableData)
            delay(1.seconds)
            component.submit()
            delay(1.seconds)
            klarnaPaymentViewCallback.captured.onAuthorized(
                klarnaPaymentView,
                true,
                "authToken",
                false,
            )
            delay(1.seconds)
            errorJob.cancel()
            validationJob.cancel()
            stepJob.cancel()

            verify(exactly = 1) {
                klarnaPaymentView.authorize(true, authorizationSessionData)
                KlarnaPaymentCategoryValidator.validate(listOf(paymentCategory), paymentCategory)
            }
            coVerify(exactly = 1) {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.KLARNA.name,
                )
                klarnaTokenizationDelegate.tokenize(any())
            }
            coVerify(exactly = 0) {
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerError>())
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerValidationError>())
            }
            assertEquals(component.isFinalizationRequired, false)
            assertEquals(emptyList<PrimerError>(), errors)
            assertEquals(
                listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                    PrimerValidationStatus.Validating(collectableData),
                    PrimerValidationStatus.Valid(collectableData),
                ),
                validationStatuses,
            )
            assertEquals(
                listOf(
                    KlarnaPaymentStep.PaymentSessionAuthorized(true),
                ),
                steps.takeLast(2),
            )
            unmockkObject(KlarnaPaymentCategoryValidator)
        }

    @Test
    fun `submit() should start authorization, log event, and emit error when payment is not approved`() =
        runTest {
            initComponent(isMockedFlow = false)
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Awaits
            val primerError = mockk<PrimerError>()
            every { errorMapperRegistry.getPrimerError(any()) } returns primerError
            mockkObject(KlarnaPaymentCategoryValidator)
            every { KlarnaPaymentCategoryValidator.validate(any(), any()) } returns null
            val klarnaPaymentViewCallback = slot<KlarnaPaymentViewCallback>()
            val klarnaPaymentView = mockk<KlarnaPaymentView>(relaxed = true)
            every {
                createKlarnaPaymentView.invoke(
                    any(),
                    any(),
                    capture(klarnaPaymentViewCallback),
                    any(),
                )
            } returns klarnaPaymentView

            val paymentMethodData = mockk<PrimerPaymentMethodTokenData>(relaxed = true)
            coEvery {
                klarnaTokenizationDelegate.tokenize(any())
            } returns Result.success(paymentMethodData)
            val paymentCategory =
                mockk<KlarnaPaymentCategory> {
                    every { identifier } returns "identifier"
                }
            val klarnaSession =
                mockk<KlarnaSession> {
                    every { sessionId } returns "sessionId"
                    every { clientToken } returns "clientToken"
                    every { availableCategories } returns listOf(paymentCategory)
                }
            component.klarnaSession = klarnaSession
            val errors = mutableListOf<PrimerError>()
            val errorJob = component.componentError.collectIn(errors, this)
            val validationStatuses =
                mutableListOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>()
            val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
            val steps = mutableListOf<KlarnaPaymentStep>()
            val stepJob = component.componentStep.collectIn(steps, this)
            val collectableData =
                KlarnaPaymentCollectableData.PaymentOptions(
                    context = mockk(),
                    returnIntentUrl = returnIntentUrl,
                    paymentCategory = paymentCategory,
                )

            component.updateCollectedData(collectableData)
            delay(1.seconds)
            component.submit()
            delay(1.seconds)
            klarnaPaymentViewCallback.captured.onAuthorized(
                klarnaPaymentView,
                false,
                null,
                true,
            )
            delay(1.seconds)
            errorJob.cancel()
            validationJob.cancel()
            stepJob.cancel()

            verify(exactly = 1) {
                klarnaPaymentView.authorize(false, authorizationSessionData)
                KlarnaPaymentCategoryValidator.validate(listOf(paymentCategory), paymentCategory)
                errorMapperRegistry.getPrimerError(any<KlarnaUserUnapprovedException>())
            }
            coVerify(exactly = 1) {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.KLARNA.name,
                )
                errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
            }
            coVerify(exactly = 0) {
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerError>())
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerValidationError>())
            }
            coVerify(exactly = 0) {
                klarnaTokenizationDelegate.tokenize(any())
            }
            assertEquals(component.isFinalizationRequired, true)
            assertEquals(listOf<PrimerError>(primerError), errors)
            assertEquals(
                listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                    PrimerValidationStatus.Validating(collectableData),
                    PrimerValidationStatus.Valid(collectableData),
                ),
                validationStatuses,
            )
            unmockkObject(KlarnaPaymentCategoryValidator)
        }

    @Test
    fun `submit() should start authorization, log event, emit error, and call error handler when payment is authorized but tokenization delegate fails`() =
        runTest {
            initComponent(isMockedFlow = false)
            coEvery { klarnaPaymentDelegate.handleError(any()) } just Runs
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Awaits
            val primerError = mockk<PrimerError>()
            every { errorMapperRegistry.getPrimerError(any()) } returns primerError
            mockkObject(KlarnaPaymentCategoryValidator)
            every { KlarnaPaymentCategoryValidator.validate(any(), any()) } returns null
            val klarnaPaymentViewCallback = slot<KlarnaPaymentViewCallback>()
            val klarnaPaymentView = mockk<KlarnaPaymentView>(relaxed = true)
            every {
                createKlarnaPaymentView.invoke(
                    any(),
                    any(),
                    capture(klarnaPaymentViewCallback),
                    any(),
                )
            } returns klarnaPaymentView
            val exception = Exception()
            coEvery { klarnaTokenizationDelegate.tokenize(any()) } returns
                Result.failure(
                    exception,
                )
            val paymentCategory =
                mockk<KlarnaPaymentCategory> {
                    every { identifier } returns "identifier"
                }
            val klarnaSession =
                mockk<KlarnaSession> {
                    every { sessionId } returns "sessionId"
                    every { clientToken } returns "clientToken"
                    every { availableCategories } returns listOf(paymentCategory)
                }
            component.klarnaSession = klarnaSession
            val errors = mutableListOf<PrimerError>()
            val errorJob = component.componentError.collectIn(errors, this)
            val validationStatuses =
                mutableListOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>()
            val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
            val steps = mutableListOf<KlarnaPaymentStep>()
            val stepJob = component.componentStep.collectIn(steps, this)
            val collectableData =
                KlarnaPaymentCollectableData.PaymentOptions(
                    context = mockk(),
                    returnIntentUrl = returnIntentUrl,
                    paymentCategory = paymentCategory,
                )

            component.updateCollectedData(collectableData)
            delay(1.seconds)
            component.submit()
            delay(1.seconds)
            klarnaPaymentViewCallback.captured.onAuthorized(
                klarnaPaymentView,
                true,
                "authToken",
                false,
            )
            delay(1.seconds)
            errorJob.cancel()
            validationJob.cancel()
            stepJob.cancel()

            verify(exactly = 1) {
                klarnaPaymentView.authorize(false, authorizationSessionData)
                KlarnaPaymentCategoryValidator.validate(listOf(paymentCategory), paymentCategory)
                errorMapperRegistry.getPrimerError(
                    withArg { it is KlarnaComponent.CheckoutFailureException && it.cause == exception },
                )
            }
            coVerify(exactly = 1) {
                klarnaPaymentDelegate.handleError(
                    withArg { it is KlarnaComponent.CheckoutFailureException && it.cause == exception },
                )
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.KLARNA.name,
                )
                klarnaTokenizationDelegate.tokenize(
                    KlarnaTokenizationInputable(
                        sessionId = "sessionId",
                        authorizationToken = "authToken",
                        paymentMethodType = PaymentMethodType.KLARNA.name,
                        primerSessionIntent = primerSessionIntent,
                    ),
                )
                errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
            }
            coVerify(exactly = 0) {
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerError>())
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerValidationError>())
            }
            assertEquals(listOf<PrimerError>(primerError), errors)
            assertEquals(
                listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                    PrimerValidationStatus.Validating(collectableData),
                    PrimerValidationStatus.Valid(collectableData),
                ),
                validationStatuses,
            )
            unmockkObject(KlarnaPaymentCategoryValidator)
        }

    @Test
    fun `submit() should start authorization, log event, and emit error when a payment error occurs`() =
        runTest {
            initComponent(isMockedFlow = false)
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Awaits
            val primerError = mockk<PrimerError>()
            every { errorMapperRegistry.getPrimerError(any()) } returns primerError
            mockkObject(KlarnaPaymentCategoryValidator)
            every { KlarnaPaymentCategoryValidator.validate(any(), any()) } returns null
            val klarnaPaymentViewCallback = slot<KlarnaPaymentViewCallback>()
            val klarnaPaymentView = mockk<KlarnaPaymentView>(relaxed = true)
            every {
                createKlarnaPaymentView.invoke(
                    any(),
                    any(),
                    capture(klarnaPaymentViewCallback),
                    any(),
                )
            } returns klarnaPaymentView
            val paymentCategory =
                mockk<KlarnaPaymentCategory> {
                    every { identifier } returns "identifier"
                }
            val klarnaSession =
                mockk<KlarnaSession> {
                    every { sessionId } returns "sessionId"
                    every { clientToken } returns "clientToken"
                    every { availableCategories } returns listOf(paymentCategory)
                }
            component.klarnaSession = klarnaSession
            val errors = mutableListOf<PrimerError>()
            val errorJob = component.componentError.collectIn(errors, this)
            val validationStatuses =
                mutableListOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>()
            val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
            val steps = mutableListOf<KlarnaPaymentStep>()
            val stepJob = component.componentStep.collectIn(steps, this)
            val collectableData =
                KlarnaPaymentCollectableData.PaymentOptions(
                    context = mockk(),
                    returnIntentUrl = returnIntentUrl,
                    paymentCategory = paymentCategory,
                )

            component.updateCollectedData(collectableData)
            delay(1.seconds)
            component.submit()
            delay(1.seconds)
            val sdkError =
                mockk<KlarnaPaymentsSDKError> {
                    every { name } returns "errorName"
                    every { message } returns "errorMessage"
                }
            klarnaPaymentViewCallback.captured.onErrorOccurred(klarnaPaymentView, sdkError)
            delay(1.seconds)
            errorJob.cancel()
            validationJob.cancel()
            stepJob.cancel()

            verify(exactly = 1) {
                klarnaPaymentView.authorize(false, authorizationSessionData)
                KlarnaPaymentCategoryValidator.validate(listOf(paymentCategory), paymentCategory)
                errorMapperRegistry.getPrimerError(
                    withArg<KlarnaSdkErrorException> {
                        assertEquals(
                            "errorName: errorMessage",
                            it.message,
                        )
                    },
                )
            }
            coVerify(exactly = 1) {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.KLARNA.name,
                )
                errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
            }
            coVerify(exactly = 0) {
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerError>())
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerValidationError>())
            }
            assertEquals(listOf<PrimerError>(primerError), errors)
            assertEquals(
                listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                    PrimerValidationStatus.Validating(collectableData),
                    PrimerValidationStatus.Valid(collectableData),
                ),
                validationStatuses,
            )
            unmockkObject(KlarnaPaymentCategoryValidator)
        }

    @Test
    fun `submit() should start authorization, log event, tokenize, handle payment token and emit finalized step when payment is finalized`() =
        runTest {
            initComponent(isMockedFlow = false)
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            mockkObject(KlarnaPaymentCategoryValidator)
            every { KlarnaPaymentCategoryValidator.validate(any(), any()) } returns null
            val klarnaPaymentViewCallback = slot<KlarnaPaymentViewCallback>()
            val klarnaPaymentView = mockk<KlarnaPaymentView>(relaxed = true)
            every {
                createKlarnaPaymentView.invoke(
                    any(),
                    any(),
                    capture(klarnaPaymentViewCallback),
                    any(),
                )
            } returns klarnaPaymentView

            val paymentMethodData = mockk<PrimerPaymentMethodTokenData>(relaxed = true)
            coEvery { klarnaTokenizationDelegate.tokenize(any()) } returns
                Result.success(
                    paymentMethodData,
                )
            val paymentDecision = mockk<PaymentDecision>(relaxed = true)
            coEvery {
                klarnaPaymentDelegate.handlePaymentMethodToken(any(), any())
            } returns Result.success(paymentDecision)

            val paymentCategory =
                mockk<KlarnaPaymentCategory> {
                    every { identifier } returns "identifier"
                }
            val klarnaSession =
                mockk<KlarnaSession> {
                    every { sessionId } returns "sessionId"
                    every { clientToken } returns "clientToken"
                    every { availableCategories } returns listOf(paymentCategory)
                }
            component.klarnaSession = klarnaSession
            val errors = mutableListOf<PrimerError>()
            val errorJob = component.componentError.collectIn(errors, this)
            val validationStatuses =
                mutableListOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>()
            val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
            val steps = mutableListOf<KlarnaPaymentStep>()
            val stepJob = component.componentStep.collectIn(steps, this)
            val collectableData =
                KlarnaPaymentCollectableData.PaymentOptions(
                    context = mockk(),
                    returnIntentUrl = returnIntentUrl,
                    paymentCategory = paymentCategory,
                )

            component.updateCollectedData(collectableData)
            delay(1.seconds)
            component.submit()
            delay(1.seconds)
            klarnaPaymentViewCallback.captured.onFinalized(
                klarnaPaymentView,
                true,
                "authToken",
            )
            delay(1.seconds)
            errorJob.cancel()
            validationJob.cancel()
            stepJob.cancel()

            verify(exactly = 1) {
                klarnaPaymentView.authorize(false, authorizationSessionData)
                KlarnaPaymentCategoryValidator.validate(listOf(paymentCategory), paymentCategory)
            }
            coVerify(exactly = 1) {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.KLARNA.name,
                )
                klarnaTokenizationDelegate.tokenize(any())
            }
            coVerify(exactly = 0) {
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerError>())
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerValidationError>())
            }
            assertEquals(component.isFinalizationRequired, false)
            assertEquals(emptyList<PrimerError>(), errors)
            assertEquals(
                listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                    PrimerValidationStatus.Validating(collectableData),
                    PrimerValidationStatus.Valid(collectableData),
                ),
                validationStatuses,
            )
            assertEquals(
                listOf(
                    KlarnaPaymentStep.PaymentSessionFinalized,
                ),
                steps.takeLast(2),
            )
            unmockkObject(KlarnaPaymentCategoryValidator)
        }

    @Test
    fun `submit() should start authorization, log event, and emit error when payment is not finalized`() =
        runTest {
            initComponent(isMockedFlow = false)
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Awaits
            val primerError = mockk<PrimerError>()
            every { errorMapperRegistry.getPrimerError(any()) } returns primerError
            mockkObject(KlarnaPaymentCategoryValidator)
            every { KlarnaPaymentCategoryValidator.validate(any(), any()) } returns null
            val klarnaPaymentViewCallback = slot<KlarnaPaymentViewCallback>()
            val klarnaPaymentView = mockk<KlarnaPaymentView>(relaxed = true)
            every {
                createKlarnaPaymentView.invoke(
                    any(),
                    any(),
                    capture(klarnaPaymentViewCallback),
                    any(),
                )
            } returns klarnaPaymentView
            val paymentCategory =
                mockk<KlarnaPaymentCategory> {
                    every { identifier } returns "identifier"
                }
            val klarnaSession =
                mockk<KlarnaSession> {
                    every { sessionId } returns "sessionId"
                    every { clientToken } returns "clientToken"
                    every { availableCategories } returns listOf(paymentCategory)
                }
            component.klarnaSession = klarnaSession
            val errors = mutableListOf<PrimerError>()
            val errorJob = component.componentError.collectIn(errors, this)
            val validationStatuses =
                mutableListOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>()
            val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
            val steps = mutableListOf<KlarnaPaymentStep>()
            val stepJob = component.componentStep.collectIn(steps, this)
            val collectableData =
                KlarnaPaymentCollectableData.PaymentOptions(
                    context = mockk(),
                    returnIntentUrl = returnIntentUrl,
                    paymentCategory = paymentCategory,
                )
            component.updateCollectedData(collectableData)

            delay(1.seconds)
            component.submit()
            delay(1.seconds)
            klarnaPaymentViewCallback.captured.onFinalized(
                klarnaPaymentView,
                false,
                null,
            )
            delay(1.seconds)
            errorJob.cancel()
            validationJob.cancel()
            stepJob.cancel()

            verify(exactly = 1) {
                klarnaPaymentView.authorize(false, authorizationSessionData)
                KlarnaPaymentCategoryValidator.validate(listOf(paymentCategory), paymentCategory)
                errorMapperRegistry.getPrimerError(any<KlarnaUserUnapprovedException>())
            }
            coVerify(exactly = 1) {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.KLARNA.name,
                )
                errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
            }
            coVerify(exactly = 0) {
                klarnaTokenizationDelegate.tokenize(any())
            }
            coVerify(exactly = 0) {
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerError>())
                validationErrorLoggingDelegate.logSdkAnalyticsError(any<PrimerValidationError>())
            }

            assertEquals(component.isFinalizationRequired, false)
            assertEquals(listOf<PrimerError>(primerError), errors)
            assertEquals(
                listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                    PrimerValidationStatus.Validating(collectableData),
                    PrimerValidationStatus.Valid(collectableData),
                ),
                validationStatuses,
            )
            unmockkObject(KlarnaPaymentCategoryValidator)
        }

    private fun initComponent(isMockedFlow: Boolean = false) {
        every { mockConfigurationDelegate.isMockedFlow() } returns isMockedFlow
        component =
            KlarnaComponent(
                tokenizationDelegate = klarnaTokenizationDelegate,
                paymentDelegate = klarnaPaymentDelegate,
                klarnaSessionCreationDelegate = klarnaSessionCreationDelegate,
                mockConfigurationDelegate = mockConfigurationDelegate,
                eventLoggingDelegate = eventLoggingDelegate,
                errorLoggingDelegate = errorLoggingDelegate,
                validationErrorLoggingDelegate = validationErrorLoggingDelegate,
                authorizationSessionDataDelegate = authorizationSessionDataDelegate,
                errorMapperRegistry = errorMapperRegistry,
                createKlarnaPaymentView = createKlarnaPaymentView,
                primerSettings = primerSettings,
                primerSessionIntent = primerSessionIntent,
            )
    }
}
