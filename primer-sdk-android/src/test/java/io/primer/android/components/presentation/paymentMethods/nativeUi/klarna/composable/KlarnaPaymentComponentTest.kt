package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.composable

import android.content.Context
import android.net.Uri
import android.net.Uri.Builder
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentView
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentViewCallback
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentsSDKError
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkConstructor
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.validation.validator.KlarnaPaymentCategoryValidator
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaPaymentCategory
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaSession
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.analytics.KlarnaPaymentAnalyticsConstants
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate.KlarnaSessionCreationDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate.KlarnaTokenizationDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models.KlarnaPaymentCollectableData
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models.KlarnaPaymentCollectableData.PaymentCategory.ReturnIntentData
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models.KlarnaPaymentStep
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.error.ErrorMapper
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.extensions.collectIn
import io.primer.android.klarna.exceptions.KlarnaSdkErrorException
import io.primer.android.klarna.exceptions.KlarnaUserUnapprovedException
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
class KlarnaPaymentComponentTest {
    private var returnIntentData = ReturnIntentData(
        scheme = "scheme",
        host = "host"
    )

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
    private lateinit var errorMapper: ErrorMapper

    private lateinit var component: KlarnaPaymentComponent

    @BeforeEach
    fun setUp() {
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "uuid"
        mockkConstructor(Uri.Builder::class)
        val builder = mockk<Builder>(relaxed = true)
        every { anyConstructed<Uri.Builder>().scheme(any()) } returns builder
        every { builder.authority(any()) } returns builder
        every { builder.build().toString() } returns "returnUrl"

        component = KlarnaPaymentComponent(
            klarnaTokenizationDelegate = klarnaTokenizationDelegate,
            klarnaSessionCreationDelegate = klarnaSessionCreationDelegate,
            headlessManagerDelegate = mockk(),
            eventLoggingDelegate = eventLoggingDelegate,
            errorLoggingDelegate = errorLoggingDelegate,
            errorMapper = errorMapper,
            createKlarnaPaymentView = createKlarnaPaymentView
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(UUID::class)
        unmockkConstructor(Uri.Builder::class)

        confirmVerified(
            klarnaTokenizationDelegate,
            klarnaSessionCreationDelegate,
            errorMapper
        )
    }

    @Test
    fun `start() should log event and emit session creation step when session creation delegate succeeds`() = runTest {
        val availableCategories = mockk<List<KlarnaPaymentCategory>>()
        val klarnaSession = mockk<KlarnaSession> {
            every { this@mockk.availableCategories } returns availableCategories
        }
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        coEvery { klarnaSessionCreationDelegate.createSession() } returns Result.success(
            klarnaSession
        )
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
            klarnaSessionCreationDelegate.createSession()
        }
        assertEquals(emptyList<PrimerError>(), errors)
        assertEquals(
            emptyList<PrimerValidationStatus<KlarnaPaymentCollectableData>>(),
            validationStatuses
        )
        assertEquals(listOf(KlarnaPaymentStep.PaymentSessionCreated(availableCategories)), steps)
    }

    @Test
    fun `start() should log event, log and emit error when session creation delegate fails`() = runTest {
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Runs
        val exception = Exception()
        coEvery { klarnaSessionCreationDelegate.createSession() } returns Result.failure(exception)
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
            errorMapper.getPrimerError(exception)
        }
        coVerify(exactly = 1) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_START_METHOD,
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
            klarnaSessionCreationDelegate.createSession()
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
            KlarnaPaymentCollectableData.PaymentCategory(mockk(), returnIntentData, paymentCategory)
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
            klarnaPaymentView.initialize("clientToken", "returnUrl")
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
        unmockkConstructor(KlarnaPaymentView::class)
    }

    @Test
    fun `updateCollectedData() should log event and emit invalid status when KlarnaPaymentCategoryValidator validate() returns validation error`() = runTest {
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        mockkObject(KlarnaPaymentCategoryValidator)
        val validationError = mockk<PrimerValidationError>()
        every { KlarnaPaymentCategoryValidator.validate(any(), any()) } returns validationError
        val paymentCategory = mockk<KlarnaPaymentCategory>()
        val collectableData =
            KlarnaPaymentCollectableData.PaymentCategory(mockk(), returnIntentData, paymentCategory)
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
    fun `submit() should emit authorization step, log event, tokenize and emit authorized step when payment is approved`() = runTest {
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
        coEvery { klarnaTokenizationDelegate.tokenize(any(), any()) } returns Result.success(Unit)
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
        val collectableData = KlarnaPaymentCollectableData.PaymentCategory(
            context = mockk(),
            returnIntentData = returnIntentData,
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
            KlarnaPaymentCategoryValidator.validate(listOf(paymentCategory), paymentCategory)
        }
        coVerify(exactly = 1) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_SUBMIT_DATA_METHOD,
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
            klarnaTokenizationDelegate.tokenize(any(), any())
        }
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
                KlarnaPaymentStep.PaymentAuthorizationRequired,
                KlarnaPaymentStep.PaymentSessionAuthorized(true)
            ),
            steps.takeLast(2)
        )
        unmockkObject(KlarnaPaymentCategoryValidator)
    }

    @Test
    fun `submit() should emit authorization step, log event and finalize when payment is not approved and finalization is required`() = runTest {
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
        coEvery { klarnaTokenizationDelegate.tokenize(any(), any()) } returns Result.success(Unit)
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
        val collectableData = KlarnaPaymentCollectableData.PaymentCategory(
            context = mockk(),
            returnIntentData = returnIntentData,
            paymentCategory = paymentCategory
        )

        component.updateCollectedData(collectableData)
        delay(1.seconds)
        component.submit()
        delay(1.seconds)
        klarnaPaymentViewCallback.captured.onAuthorized(
            klarnaPaymentView,
            false,
            "authToken",
            true
        )
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        verify(exactly = 1) {
            KlarnaPaymentCategoryValidator.validate(listOf(paymentCategory), paymentCategory)
            klarnaPaymentView.finalize(null)
        }
        coVerify(exactly = 1) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_SUBMIT_DATA_METHOD,
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
        }
        coVerify(exactly = 0) {
            klarnaTokenizationDelegate.tokenize(any(), any())
        }
        assertEquals(emptyList<PrimerError>(), errors)
        assertEquals(
            listOf<PrimerValidationStatus<KlarnaPaymentCollectableData>>(
                PrimerValidationStatus.Validating(collectableData),
                PrimerValidationStatus.Valid(collectableData)
            ),
            validationStatuses
        )
        assertEquals(KlarnaPaymentStep.PaymentAuthorizationRequired, steps.last())
        unmockkObject(KlarnaPaymentCategoryValidator)
    }

    @Test
    fun `submit() should log event, and emit error when payment is not authorized`() = runTest {
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
        coEvery { klarnaTokenizationDelegate.tokenize(any(), any()) } returns Result.success(Unit)
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
        val collectableData = KlarnaPaymentCollectableData.PaymentCategory(
            context = mockk(),
            returnIntentData = returnIntentData,
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
            false
        )
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        verify(exactly = 1) {
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
            klarnaTokenizationDelegate.tokenize(any(), any())
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
    fun `submit() should log event, and emit error when payment is authorized but tokenization delegate fails`() = runTest {
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
        coEvery { klarnaTokenizationDelegate.tokenize(any(), any()) } returns Result.failure(
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
        val collectableData = KlarnaPaymentCollectableData.PaymentCategory(
            context = mockk(),
            returnIntentData = returnIntentData,
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
            KlarnaPaymentCategoryValidator.validate(listOf(paymentCategory), paymentCategory)
            errorMapper.getPrimerError(exception)
        }
        coVerify(exactly = 1) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_SUBMIT_DATA_METHOD,
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
            klarnaTokenizationDelegate.tokenize("sessionId", "authToken")
            errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
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
    fun `submit() should log event, and emit error when a payment error occurs`() = runTest {
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
        val collectableData = KlarnaPaymentCollectableData.PaymentCategory(
            context = mockk(),
            returnIntentData = returnIntentData,
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
    fun `submit() should eit authorization step, log event, tokenize and emit finalized step when payment is finalized`() = runTest {
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
        coEvery { klarnaTokenizationDelegate.tokenize(any(), any()) } returns Result.success(Unit)
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
        val collectableData = KlarnaPaymentCollectableData.PaymentCategory(
            context = mockk(),
            returnIntentData = returnIntentData,
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
            KlarnaPaymentCategoryValidator.validate(listOf(paymentCategory), paymentCategory)
        }
        coVerify(exactly = 1) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_SUBMIT_DATA_METHOD,
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
            klarnaTokenizationDelegate.tokenize(any(), any())
        }
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
                KlarnaPaymentStep.PaymentAuthorizationRequired,
                KlarnaPaymentStep.PaymentSessionFinalized
            ),
            steps.takeLast(2)
        )
        unmockkObject(KlarnaPaymentCategoryValidator)
    }

    @Test
    fun `submit() should log event, and emit error when payment is not finalized`() = runTest {
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
        val collectableData = KlarnaPaymentCollectableData.PaymentCategory(
            context = mockk(),
            returnIntentData = returnIntentData,
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
            klarnaTokenizationDelegate.tokenize(any(), any())
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
}
