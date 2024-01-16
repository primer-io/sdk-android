package io.primer.android.components.manager.banks.component

import androidx.lifecycle.SavedStateHandle
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
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.banks.validation.validator.BankIdValidator
import io.primer.android.components.domain.payments.paymentMethods.banks.validation.validator.BanksValidations
import io.primer.android.components.manager.banks.analytics.BanksAnalyticsConstants
import io.primer.android.components.manager.banks.composable.BanksCollectableData
import io.primer.android.components.manager.banks.composable.BanksStep
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.componentWithRedirect.banks.delegate.BankIssuerTokenizationDelegate
import io.primer.android.components.presentation.paymentMethods.componentWithRedirect.banks.delegate.GetBanksDelegate
import io.primer.android.domain.error.ErrorMapper
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.rpc.banks.models.IssuingBank
import io.primer.android.extensions.collectIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
class DefaultBanksComponentTest {
    private val bankId = "bankId"

    private val paymentMethodType = "paymentMethodType"

    @MockK
    private lateinit var getBanksDelegate: GetBanksDelegate

    @MockK
    private lateinit var bankIssuerTokenizationDelegate: BankIssuerTokenizationDelegate

    @MockK
    private lateinit var eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate

    @MockK
    private lateinit var errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate

    @MockK
    private lateinit var errorMapper: ErrorMapper

    @MockK
    private lateinit var savedStateHandle: SavedStateHandle

    @RelaxedMockK
    private lateinit var onFinished: () -> Unit

    @RelaxedMockK
    private lateinit var onDisposed: () -> Unit

    private lateinit var component: DefaultBanksComponent

    @BeforeEach
    fun setUp() {
        every { savedStateHandle.get<String>("bank_id") } returns bankId
        every { savedStateHandle.set<String>("bank_id", bankId) } just Runs
        component = DefaultBanksComponent(
            paymentMethodType = paymentMethodType,
            getBanksDelegate = getBanksDelegate,
            bankIssuerTokenizationDelegate = bankIssuerTokenizationDelegate,
            eventLoggingDelegate = eventLoggingDelegate,
            errorLoggingDelegate = errorLoggingDelegate,
            errorMapper = errorMapper,
            savedStateHandle = savedStateHandle,
            onFinished = onFinished,
            onDisposed = onDisposed
        )
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "uuid"
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(UUID::class)

        confirmVerified(
            getBanksDelegate,
            bankIssuerTokenizationDelegate,
            eventLoggingDelegate,
            errorLoggingDelegate,
            errorMapper,
            onFinished,
            onDisposed
        )
    }

    @Test
    fun `start() should log event, emit loading step and bank list when GetBanksDelegate succeeds`() = runTest {
        val banks = mockk<List<IssuingBank>>()
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        coEvery { getBanksDelegate.getBanks(any()) } returns Result.success(banks)
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses = mutableListOf<PrimerValidationStatus<BanksCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<BanksStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        component.start()
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        coVerify(exactly = 1) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = BanksAnalyticsConstants.BANKS_START_METHOD,
                paymentMethodType = paymentMethodType
            )
            getBanksDelegate.getBanks(query = null)
        }
        assertEquals(emptyList(), errors)
        assertEquals(emptyList(), validationStatuses)
        assertEquals(listOf(BanksStep.Loading, BanksStep.BanksRetrieved(banks)), steps)
    }

    @Test
    fun `start() should log event, emit loading step, log and emit error when GetBanksDelegate fails`() = runTest {
        val exception = Exception()
        val primerError = mockk<PrimerError>()
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Runs
        coEvery { getBanksDelegate.getBanks(any()) } returns Result.failure(exception)
        coEvery { errorMapper.getPrimerError(any()) } returns primerError
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses = mutableListOf<PrimerValidationStatus<BanksCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<BanksStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        component.start()
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        coVerify(exactly = 1) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = BanksAnalyticsConstants.BANKS_START_METHOD,
                paymentMethodType = paymentMethodType
            )
            getBanksDelegate.getBanks(query = null)
            errorMapper.getPrimerError(exception)
            errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
        }
        assertEquals(listOf(primerError), errors)
        assertEquals(emptyList(), validationStatuses)
        assertEquals(listOf<BanksStep>(BanksStep.Loading), steps)
    }

    @Test
    fun `updateCollectedData() should log event, debounce input and emit valid status when BankIdValidator validate() returns null`() = runTest {
        mockkObject(BankIdValidator)
        every { BankIdValidator.validate(any(), any()) } returns null
        val banks = listOf(mockk<IssuingBank>() { every { id } returns bankId })
        component.banks = banks
        val collectableData = BanksCollectableData.BankId(bankId)
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses = mutableListOf<PrimerValidationStatus<BanksCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<BanksStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        repeat(3) {
            component.updateCollectedData(collectableData)
            delay(100.milliseconds)
        }
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        coVerify(exactly = 3) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = BanksAnalyticsConstants.BANKS_COLLECTED_DATA_METHOD,
                paymentMethodType = paymentMethodType
            )
        }
        assertEquals(emptyList(), errors)
        assertEquals(
            listOf(
                PrimerValidationStatus.Validating(collectableData),
                PrimerValidationStatus.Valid(collectableData)
            ),
            validationStatuses
        )
        assertEquals(emptyList(), steps)
        unmockkObject(BankIdValidator)
    }

    @Test
    fun `updateCollectedData() should log event, debounce input, emit invalid status when called with query before bank list is loaded`() = runTest {
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        val banks = mockk<List<IssuingBank>>()
        val collectableData = BanksCollectableData.Filter("query")
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses = mutableListOf<PrimerValidationStatus<BanksCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<BanksStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        repeat(3) {
            component.updateCollectedData(collectableData)
            delay(100.milliseconds)
        }
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        coVerify(exactly = 3) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = BanksAnalyticsConstants.BANKS_COLLECTED_DATA_METHOD,
                paymentMethodType = paymentMethodType
            )
        }
        coVerify(exactly = 0) {
            getBanksDelegate.getBanks(query = any())
        }
        assertEquals(emptyList(), errors)
        assertEquals(
            listOf(
                PrimerValidationStatus.Validating(collectableData),
                PrimerValidationStatus.Invalid(
                    listOf(
                        PrimerValidationError(
                            errorId = BanksValidations.BANKS_NOT_LOADED_ERROR_ID,
                            description = "Banks need to be loaded before bank id can be collected."
                        )
                    ),
                    collectableData
                )
            ),
            validationStatuses
        )
        assertEquals(emptyList(), steps)
    }

    @Test
    fun `updateCollectedData() should log event, debounce input, emit valid status and bank list when called with query`() = runTest {
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        val banks = mockk<List<IssuingBank>>()
        coEvery { getBanksDelegate.getBanks(any()) } returns Result.success(banks)
        val collectableData = BanksCollectableData.Filter("query")
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses = mutableListOf<PrimerValidationStatus<BanksCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<BanksStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        component.start()
        delay(1.seconds)
        repeat(3) {
            component.updateCollectedData(collectableData)
            delay(100.milliseconds)
        }
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        coVerify(exactly = 1) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = BanksAnalyticsConstants.BANKS_START_METHOD,
                paymentMethodType = paymentMethodType
            )
        }
        coVerify(exactly = 3) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = BanksAnalyticsConstants.BANKS_COLLECTED_DATA_METHOD,
                paymentMethodType = paymentMethodType
            )
        }
        coVerify(exactly = 1) {
            getBanksDelegate.getBanks(query = null)
        }
        coVerify(exactly = 1) {
            getBanksDelegate.getBanks(query = "query")
        }
        assertEquals(emptyList(), errors)
        assertEquals(
            listOf(
                PrimerValidationStatus.Validating(collectableData),
                PrimerValidationStatus.Valid(collectableData)
            ),
            validationStatuses
        )
        assertEquals(listOf(BanksStep.Loading, BanksStep.BanksRetrieved(banks)), steps.takeLast(2))
    }

    @Test
    fun `updateCollectedData() should log event and emit invalid status when BankIdValidator validate() returns error`() {
        runTest {
            mockkObject(BankIdValidator)
            val validationError = mockk<PrimerValidationError>()
            every { BankIdValidator.validate(any(), any()) } returns validationError
            val collectableData = BanksCollectableData.BankId(bankId)
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            val errors = mutableListOf<PrimerError>()
            val errorJob = component.componentError.collectIn(errors, this)
            val validationStatuses = mutableListOf<PrimerValidationStatus<BanksCollectableData>>()
            val validationJob =
                component.componentValidationStatus.collectIn(validationStatuses, this)
            val steps = mutableListOf<BanksStep>()
            val stepJob = component.componentStep.collectIn(steps, this)

            component.updateCollectedData(collectableData)
            delay(1.seconds)
            errorJob.cancel()
            validationJob.cancel()
            stepJob.cancel()

            coVerify(exactly = 1) {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName = BanksAnalyticsConstants.BANKS_COLLECTED_DATA_METHOD,
                    paymentMethodType = paymentMethodType
                )
            }
            assertEquals(emptyList(), errors)
            assertEquals(
                listOf(
                    PrimerValidationStatus.Validating(collectableData),
                    PrimerValidationStatus.Invalid(
                        validationErrors = listOf(validationError),
                        collectableData = collectableData
                    )
                ),
                validationStatuses
            )
            assertEquals(emptyList(), steps)
            unmockkObject(BankIdValidator)
        }
    }

    @Test
    fun `submit() should log event, tokenize and call onFinished() when BankIssuerTokenizationDelegate succeeds`() = runTest {
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        coEvery { bankIssuerTokenizationDelegate.tokenize(any()) } returns Result.success(Unit)
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses = mutableListOf<PrimerValidationStatus<BanksCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<BanksStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        component.submit()
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        coVerify(exactly = 1) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = BanksAnalyticsConstants.BANKS_SUBMIT_DATA_METHOD,
                paymentMethodType = paymentMethodType
            )
            bankIssuerTokenizationDelegate.tokenize(bankId)
            onFinished()
        }
        assertEquals(emptyList(), errors)
        assertEquals(emptyList(), validationStatuses)
        assertEquals(emptyList(), steps)
    }

    @Test
    fun `submit() should log event and error when BankIssuerTokenizationDelegate fails`() = runTest {
        val exception = Exception()
        val primerError = mockk<PrimerError>()
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Runs
        coEvery { errorMapper.getPrimerError(any()) } returns primerError
        coEvery { bankIssuerTokenizationDelegate.tokenize(any()) } returns Result.failure(exception)
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses = mutableListOf<PrimerValidationStatus<BanksCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<BanksStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        component.submit()
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        coVerify(exactly = 1) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = BanksAnalyticsConstants.BANKS_SUBMIT_DATA_METHOD,
                paymentMethodType = paymentMethodType
            )
            bankIssuerTokenizationDelegate.tokenize(bankId)
            errorMapper.getPrimerError(exception)
            errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
        }
        verify(exactly = 0) {
            onFinished()
        }
        assertEquals(listOf(primerError), errors)
        assertEquals(emptyList(), validationStatuses)
        assertEquals(emptyList(), steps)
    }
}
