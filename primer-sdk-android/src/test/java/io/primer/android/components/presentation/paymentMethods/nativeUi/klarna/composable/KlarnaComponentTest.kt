package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.composable

import android.content.Context
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentView
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentViewCallback
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentsSDKError
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.error.KlarnaSdkErrorException
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.error.KlarnaUserUnapprovedException
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.validation.validator.KlarnaPaymentCategoryValidator
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.validation.validator.KlarnaPaymentFinalizationValidator
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaPaymentCategory
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaSession
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.components.presentation.mock.delegate.MockConfigurationDelegate
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.analytics.KlarnaPaymentAnalyticsConstants
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate.GetKlarnaAuthorizationSessionDataDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate.KlarnaSessionCreationDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate.KlarnaTokenizationDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models.KlarnaPaymentCollectableData
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models.KlarnaPaymentStep
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapper
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.extensions.collectIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
    private lateinit var createKlarnaPaymentView:
        (Context, String, KlarnaPaymentViewCallback, String) -> KlarnaPaymentView

    @MockK
    private lateinit var klarnaTokenizationDelegate: KlarnaTokenizationDelegate

    @MockK
    private lateinit var klarnaSessionCreationDelegate: KlarnaSessionCreationDelegate

    @MockK
    private lateinit var eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate

    @MockK
    private lateinit var errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate

    @MockK
    private lateinit var authorizationSessionDataDelegate: GetKlarnaAuthorizationSessionDataDelegate

    @RelaxedMockK
    private lateinit var baseErrorEventResolver: BaseErrorEventResolver

    @MockK
    private lateinit var errorMapper: ErrorMapper

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

        val mockConfigurationDelegate = mockk<MockConfigurationDelegate> {
            every { isMockedFlow() } returns false
        }
        component = KlarnaComponent(
            klarnaTokenizationDelegate = klarnaTokenizationDelegate,
            klarnaSessionCreationDelegate = klarnaSessionCreationDelegate,
            headlessManagerDelegate = mockk(),
            mockConfigurationDelegate = mockConfigurationDelegate,
            eventLoggingDelegate = eventLoggingDelegate,
            errorLoggingDelegate = errorLoggingDelegate,
            authorizationSessionDataDelegate = authorizationSessionDataDelegate,
            errorEventResolver = baseErrorEventResolver,
            errorMapper = errorMapper,
            createKlarnaPaymentView = createKlarnaPaymentView,
            primerSettings = primerSettings,
            primerSessionIntent = primerSessionIntent
        )

        confirmVerified(mockConfigurationDelegate)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(UUID::class)

        confirmVerified(
            klarnaTokenizationDelegate,
            klarnaSessionCreationDelegate,
            errorMapper
        )
    }

    @Test
    fun `start() should log event and emit session creation step when session creation delegate succeeds`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        val availableCategories = mockk<List<KlarnaPaymentCategory>>()
        val klarnaSession = mockk<KlarnaSession> {
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

        coVerify(exactly = 1) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_START_METHOD,
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
            klarnaSessionCreationDelegate.createSession(primerSessionIntent)
        }
        assertEquals(emptyList<PrimerError>(), errors)
        assertEquals(
            emptyList<PrimerValidationStatus<KlarnaPaymentCollectableData>>(),
            validationStatuses
        )
        assertEquals(listOf(KlarnaPaymentStep.PaymentSessionCreated(availableCategories)), steps)
    }

    @Test
    fun `start() should log event, log and emit error when session creation delegate fails and in HEADLESS mode`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Runs
        val exception = Exception()
        coEvery {
            klarnaSessionCreationDelegate.createSession(primerSessionIntent)
        } returns Result.failure(exception)
        val primerError = mockk<PrimerError>()
        coEvery { errorMapper.getPrimerError(any()) } returns primerError
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

        verify(exactly = 0) {
            baseErrorEventResolver.resolve(any(), any())
        }
        verify(exactly = 1) {
            errorMapper.getPrimerError(exception)
        }
        coVerify(exactly = 1) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_START_METHOD,
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
            klarnaSessionCreationDelegate.createSession(primerSessionIntent)
            errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
        }
        assertEquals(listOf(primerError), errors)
        assertEquals(
            emptyList<PrimerValidationStatus<KlarnaPaymentCollectableData>>(),
            validationStatuses
        )
        assertEquals(emptyList<KlarnaPaymentStep>(), steps)
    }

    @Test
    fun `start() should log event, log, emit error, and resolve checkout errors when session creation delegate fails and in DROP-in mode`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.DROP_IN
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Runs
        val exception = Exception()
        coEvery {
            klarnaSessionCreationDelegate.createSession(primerSessionIntent)
        } returns Result.failure(exception)
        val primerError = mockk<PrimerError>()
        coEvery { errorMapper.getPrimerError(any()) } returns primerError
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
            baseErrorEventResolver.resolve(exception, ErrorMapperType.KLARNA)
            errorMapper.getPrimerError(exception)
        }
        coVerify(exactly = 1) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_START_METHOD,
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
            klarnaSessionCreationDelegate.createSession(primerSessionIntent)
            errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
        }
        assertEquals(listOf(primerError), errors)
        assertEquals(
            emptyList<PrimerValidationStatus<KlarnaPaymentCollectableData>>(),
            validationStatuses
        )
        assertEquals(emptyList<KlarnaPaymentStep>(), steps)
    }

    @Test
    fun `updateCollectedData() should log event and emit payment view loaded step and valid status when KlarnaPaymentCategoryValidator validate() returns null`() = runTest {
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
                any()
            )
        } returns klarnaPaymentView
        val paymentCategory = mockk<KlarnaPaymentCategory> {
            every { identifier } returns "identifier"
        }
        val klarnaSession = mockk<KlarnaSession> {
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
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
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
                PrimerValidationStatus.Valid(collectableData)
            ),
            validationStatuses
        )
        assertInstanceOf(KlarnaPaymentStep.PaymentViewLoaded::class.java, steps.last())
        unmockkObject(KlarnaPaymentCategoryValidator)
    }

    @Test
    fun `updateCollectedData() should log event and emit valid status when KlarnaPaymentFinalizationValidator validate() returns null`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        mockkObject(KlarnaPaymentFinalizationValidator)
        every { KlarnaPaymentFinalizationValidator.validate(any()) } returns null
        component.isFinalizationRequired = true
        val paymentCategory = mockk<KlarnaPaymentCategory> {
            every { identifier } returns "identifier"
        }
        val klarnaSession = mockk<KlarnaSession> {
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
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
        }
        verify(exactly = 1) {
            KlarnaPaymentFinalizationValidator.validate(isFinalizationRequired = true)
        }
        assertEquals(emptyList<PrimerError>(), errors)
        assertEquals(
            listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                PrimerValidationStatus.Validating(collectableData),
                PrimerValidationStatus.Valid(collectableData)
            ),
            validationStatuses
        )
        assertEquals(emptyList<KlarnaPaymentStep>(), steps)
        unmockkObject(KlarnaPaymentFinalizationValidator)
    }

    @Test
    fun `updateCollectedData() should log event and emit invalid status when KlarnaPaymentFinalizationValidator validate() returns validation error`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
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
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
        }
        assertEquals(emptyList<PrimerError>(), errors)
        assertEquals(
            listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                PrimerValidationStatus.Validating(collectableData),
                PrimerValidationStatus.Invalid(listOf(validationError), collectableData)
            ),
            validationStatuses
        )
        assertEquals(emptyList<KlarnaPaymentStep>(), steps)
        unmockkStatic(KlarnaPaymentFinalizationValidator::class)
    }

    @Test
    fun `updateCollectedData() should log event and emit invalid status when KlarnaPaymentCategoryValidator validate() returns validation error`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
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
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
        }
        coVerify(exactly = 0) {
            createKlarnaPaymentView.invoke(any(), any(), any(), any())
        }
        assertEquals(emptyList<PrimerError>(), errors)
        assertEquals(
            listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                PrimerValidationStatus.Validating(collectableData),
                PrimerValidationStatus.Invalid(listOf(validationError), collectableData)
            ),
            validationStatuses
        )
        assertEquals(emptyList<KlarnaPaymentStep>(), steps)
        unmockkStatic(KlarnaPaymentCategoryValidator::class)
    }

    @Test
    fun `submit() should start authorization without auto finalization, log event, tokenize and emit authorized step when payment is approved when integration type is HEADLESS`() = runTest {
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
                any()
            )
        } returns klarnaPaymentView
        coEvery {
            klarnaTokenizationDelegate.tokenize(any(), any(), any())
        } returns Result.success(Unit)
        val paymentCategory = mockk<KlarnaPaymentCategory> {
            every { identifier } returns "identifier"
        }
        val klarnaSession = mockk<KlarnaSession> {
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
        val collectableData = KlarnaPaymentCollectableData.PaymentOptions(
            context = mockk(),
            returnIntentUrl = returnIntentUrl,
            paymentCategory = paymentCategory
        )

        component.updateCollectedData(collectableData)
        delay(1.seconds)
        component.submit()
        delay(1.seconds)
        klarnaPaymentViewCallback.captured.onAuthorized(
            klarnaPaymentView,
            true,
            "authToken",
            false
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
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
            klarnaTokenizationDelegate.tokenize(any(), any(), primerSessionIntent)
        }
        assertEquals(component.isFinalizationRequired, false)
        assertEquals(emptyList<PrimerError>(), errors)
        assertEquals(
            listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                PrimerValidationStatus.Validating(collectableData),
                PrimerValidationStatus.Valid(collectableData)
            ),
            validationStatuses
        )
        assertEquals(
            listOf(
                KlarnaPaymentStep.PaymentSessionAuthorized(true)
            ),
            steps.takeLast(2)
        )
        unmockkObject(KlarnaPaymentCategoryValidator)
    }

    @Test
    fun `submit() should start authorization with auto finalization, log event, tokenize and emit authorized step when payment is approved when integration type is DROP-IN`() = runTest {
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
                any()
            )
        } returns klarnaPaymentView
        coEvery {
            klarnaTokenizationDelegate.tokenize(any(), any(), any())
        } returns Result.success(Unit)
        val paymentCategory = mockk<KlarnaPaymentCategory> {
            every { identifier } returns "identifier"
        }
        val klarnaSession = mockk<KlarnaSession> {
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
        val collectableData = KlarnaPaymentCollectableData.PaymentOptions(
            context = mockk(),
            returnIntentUrl = returnIntentUrl,
            paymentCategory = paymentCategory
        )

        component.updateCollectedData(collectableData)
        delay(1.seconds)
        component.submit()
        delay(1.seconds)
        klarnaPaymentViewCallback.captured.onAuthorized(
            klarnaPaymentView,
            true,
            "authToken",
            false
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
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
            klarnaTokenizationDelegate.tokenize(any(), any(), primerSessionIntent)
        }
        assertEquals(component.isFinalizationRequired, false)
        assertEquals(emptyList<PrimerError>(), errors)
        assertEquals(
            listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                PrimerValidationStatus.Validating(collectableData),
                PrimerValidationStatus.Valid(collectableData)
            ),
            validationStatuses
        )
        assertEquals(
            listOf(
                KlarnaPaymentStep.PaymentSessionAuthorized(true)
            ),
            steps.takeLast(2)
        )
        unmockkObject(KlarnaPaymentCategoryValidator)
    }

    @Test
    fun `submit() should start authorization, log event, and emit error when payment is not approved`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Runs
        val primerError = mockk<PrimerError>()
        every { errorMapper.getPrimerError(any()) } returns primerError
        mockkObject(KlarnaPaymentCategoryValidator)
        every { KlarnaPaymentCategoryValidator.validate(any(), any()) } returns null
        val klarnaPaymentViewCallback = slot<KlarnaPaymentViewCallback>()
        val klarnaPaymentView = mockk<KlarnaPaymentView>(relaxed = true)
        every {
            createKlarnaPaymentView.invoke(
                any(),
                any(),
                capture(klarnaPaymentViewCallback),
                any()
            )
        } returns klarnaPaymentView
        coEvery {
            klarnaTokenizationDelegate.tokenize(any(), any(), any())
        } returns Result.success(Unit)
        val paymentCategory = mockk<KlarnaPaymentCategory> {
            every { identifier } returns "identifier"
        }
        val klarnaSession = mockk<KlarnaSession> {
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
        val collectableData = KlarnaPaymentCollectableData.PaymentOptions(
            context = mockk(),
            returnIntentUrl = returnIntentUrl,
            paymentCategory = paymentCategory
        )

        component.updateCollectedData(collectableData)
        delay(1.seconds)
        component.submit()
        delay(1.seconds)
        klarnaPaymentViewCallback.captured.onAuthorized(
            klarnaPaymentView,
            false,
            null,
            true
        )
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        verify(exactly = 1) {
            klarnaPaymentView.authorize(false, authorizationSessionData)
            KlarnaPaymentCategoryValidator.validate(listOf(paymentCategory), paymentCategory)
            errorMapper.getPrimerError(any<KlarnaUserUnapprovedException>())
        }
        coVerify(exactly = 1) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_SUBMIT_DATA_METHOD,
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
            errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
        }
        verify(exactly = 0) {
            baseErrorEventResolver.resolve(any(), any())
        }
        coVerify(exactly = 0) {
            klarnaTokenizationDelegate.tokenize(any(), any(), primerSessionIntent)
        }
        assertEquals(component.isFinalizationRequired, true)
        assertEquals(listOf<PrimerError>(primerError), errors)
        assertEquals(
            listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                PrimerValidationStatus.Validating(collectableData),
                PrimerValidationStatus.Valid(collectableData)
            ),
            validationStatuses
        )
        unmockkObject(KlarnaPaymentCategoryValidator)
    }

    @Test
    fun `submit() should start authorization, log event, and emit error when payment is authorized but tokenization delegate fails`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Runs
        val primerError = mockk<PrimerError>()
        every { errorMapper.getPrimerError(any()) } returns primerError
        mockkObject(KlarnaPaymentCategoryValidator)
        every { KlarnaPaymentCategoryValidator.validate(any(), any()) } returns null
        val klarnaPaymentViewCallback = slot<KlarnaPaymentViewCallback>()
        val klarnaPaymentView = mockk<KlarnaPaymentView>(relaxed = true)
        every {
            createKlarnaPaymentView.invoke(
                any(),
                any(),
                capture(klarnaPaymentViewCallback),
                any()
            )
        } returns klarnaPaymentView
        val exception = Exception()
        coEvery { klarnaTokenizationDelegate.tokenize(any(), any(), any()) } returns Result.failure(
            exception
        )
        val paymentCategory = mockk<KlarnaPaymentCategory> {
            every { identifier } returns "identifier"
        }
        val klarnaSession = mockk<KlarnaSession> {
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
        val collectableData = KlarnaPaymentCollectableData.PaymentOptions(
            context = mockk(),
            returnIntentUrl = returnIntentUrl,
            paymentCategory = paymentCategory
        )

        component.updateCollectedData(collectableData)
        delay(1.seconds)
        component.submit()
        delay(1.seconds)
        klarnaPaymentViewCallback.captured.onAuthorized(
            klarnaPaymentView,
            true,
            "authToken",
            false
        )
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        verify(exactly = 1) {
            klarnaPaymentView.authorize(false, authorizationSessionData)
            KlarnaPaymentCategoryValidator.validate(listOf(paymentCategory), paymentCategory)
            errorMapper.getPrimerError(exception)
        }
        coVerify(exactly = 1) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_SUBMIT_DATA_METHOD,
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
            klarnaTokenizationDelegate.tokenize("sessionId", "authToken", primerSessionIntent)
            errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
        }
        verify(exactly = 0) {
            baseErrorEventResolver.resolve(any(), any())
        }
        assertEquals(listOf<PrimerError>(primerError), errors)
        assertEquals(
            listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                PrimerValidationStatus.Validating(collectableData),
                PrimerValidationStatus.Valid(collectableData)
            ),
            validationStatuses
        )
        unmockkObject(KlarnaPaymentCategoryValidator)
    }

    @Test
    fun `submit() should start authorization, log event, and emit error when a payment error occurs`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Runs
        val primerError = mockk<PrimerError>()
        every { errorMapper.getPrimerError(any()) } returns primerError
        mockkObject(KlarnaPaymentCategoryValidator)
        every { KlarnaPaymentCategoryValidator.validate(any(), any()) } returns null
        val klarnaPaymentViewCallback = slot<KlarnaPaymentViewCallback>()
        val klarnaPaymentView = mockk<KlarnaPaymentView>(relaxed = true)
        every {
            createKlarnaPaymentView.invoke(
                any(),
                any(),
                capture(klarnaPaymentViewCallback),
                any()
            )
        } returns klarnaPaymentView
        val paymentCategory = mockk<KlarnaPaymentCategory> {
            every { identifier } returns "identifier"
        }
        val klarnaSession = mockk<KlarnaSession> {
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
        val collectableData = KlarnaPaymentCollectableData.PaymentOptions(
            context = mockk(),
            returnIntentUrl = returnIntentUrl,
            paymentCategory = paymentCategory
        )

        component.updateCollectedData(collectableData)
        delay(1.seconds)
        component.submit()
        delay(1.seconds)
        val sdkError = mockk<KlarnaPaymentsSDKError> {
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
            errorMapper.getPrimerError(
                withArg<KlarnaSdkErrorException> {
                    assertEquals(
                        "errorName: errorMessage",
                        it.message
                    )
                }
            )
        }
        coVerify(exactly = 1) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_SUBMIT_DATA_METHOD,
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
            errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
        }
        verify(exactly = 0) {
            baseErrorEventResolver.resolve(any(), any())
        }
        assertEquals(listOf<PrimerError>(primerError), errors)
        assertEquals(
            listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                PrimerValidationStatus.Validating(collectableData),
                PrimerValidationStatus.Valid(collectableData)
            ),
            validationStatuses
        )
        unmockkObject(KlarnaPaymentCategoryValidator)
    }

    @Test
    fun `submit() should start authorization, log event, tokenize and emit finalized step when payment is finalized`() = runTest {
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
                any()
            )
        } returns klarnaPaymentView
        coEvery { klarnaTokenizationDelegate.tokenize(any(), any(), any()) } returns Result.success(
            Unit
        )
        val paymentCategory = mockk<KlarnaPaymentCategory> {
            every { identifier } returns "identifier"
        }
        val klarnaSession = mockk<KlarnaSession> {
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
        val collectableData = KlarnaPaymentCollectableData.PaymentOptions(
            context = mockk(),
            returnIntentUrl = returnIntentUrl,
            paymentCategory = paymentCategory
        )

        component.updateCollectedData(collectableData)
        delay(1.seconds)
        component.submit()
        delay(1.seconds)
        klarnaPaymentViewCallback.captured.onFinalized(
            klarnaPaymentView,
            true,
            "authToken"
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
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
            klarnaTokenizationDelegate.tokenize(any(), any(), primerSessionIntent)
        }
        assertEquals(component.isFinalizationRequired, false)
        assertEquals(emptyList<PrimerError>(), errors)
        assertEquals(
            listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                PrimerValidationStatus.Validating(collectableData),
                PrimerValidationStatus.Valid(collectableData)
            ),
            validationStatuses
        )
        assertEquals(
            listOf(
                KlarnaPaymentStep.PaymentSessionFinalized
            ),
            steps.takeLast(2)
        )
        unmockkObject(KlarnaPaymentCategoryValidator)
    }

    @Test
    fun `submit() should start authorization, log event, and emit error when payment is not finalized`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Runs
        val primerError = mockk<PrimerError>()
        every { errorMapper.getPrimerError(any()) } returns primerError
        mockkObject(KlarnaPaymentCategoryValidator)
        every { KlarnaPaymentCategoryValidator.validate(any(), any()) } returns null
        val klarnaPaymentViewCallback = slot<KlarnaPaymentViewCallback>()
        val klarnaPaymentView = mockk<KlarnaPaymentView>(relaxed = true)
        every {
            createKlarnaPaymentView.invoke(
                any(),
                any(),
                capture(klarnaPaymentViewCallback),
                any()
            )
        } returns klarnaPaymentView
        val paymentCategory = mockk<KlarnaPaymentCategory> {
            every { identifier } returns "identifier"
        }
        val klarnaSession = mockk<KlarnaSession> {
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
        val collectableData = KlarnaPaymentCollectableData.PaymentOptions(
            context = mockk(),
            returnIntentUrl = returnIntentUrl,
            paymentCategory = paymentCategory
        )
        component.updateCollectedData(collectableData)

        delay(1.seconds)
        component.submit()
        delay(1.seconds)
        klarnaPaymentViewCallback.captured.onFinalized(
            klarnaPaymentView,
            false,
            null
        )
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        verify(exactly = 1) {
            klarnaPaymentView.authorize(false, authorizationSessionData)
            KlarnaPaymentCategoryValidator.validate(listOf(paymentCategory), paymentCategory)
            errorMapper.getPrimerError(any<KlarnaUserUnapprovedException>())
        }
        coVerify(exactly = 1) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_SUBMIT_DATA_METHOD,
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
            errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
        }
        coVerify(exactly = 0) {
            klarnaTokenizationDelegate.tokenize(any(), any(), primerSessionIntent)
        }
        verify(exactly = 0) {
            baseErrorEventResolver.resolve(any(), any())
        }
        assertEquals(component.isFinalizationRequired, false)
        assertEquals(listOf<PrimerError>(primerError), errors)
        assertEquals(
            listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                PrimerValidationStatus.Validating(collectableData),
                PrimerValidationStatus.Valid(collectableData)
            ),
            validationStatuses
        )
        unmockkObject(KlarnaPaymentCategoryValidator)
    }
}
