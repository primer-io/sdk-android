package io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach

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
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.validation.validator.EmailAddressValidator
import io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.validation.validator.FirstNameValidator
import io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.validation.validator.LastNameValidator
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.analytics.StripeAchUserDetailsAnalyticsConstants
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.composable.AchUserDetailsCollectableData
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.composable.AchUserDetailsStep
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.GetClientSessionCustomerDetailsDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.StripeAchClientSessionPatchDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.StripeAchTokenizationDelegate
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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@Suppress("LongLines")
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
class StripeAchUserDetailsComponentTest {
    @MockK
    private lateinit var getClientSessionCustomerDetailsDelegate:
        GetClientSessionCustomerDetailsDelegate

    @MockK
    private lateinit var stripeAchClientSessionPatchDelegate: StripeAchClientSessionPatchDelegate

    @MockK
    private lateinit var stripeAchTokenizationDelegate: StripeAchTokenizationDelegate

    @MockK
    private lateinit var eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate

    @MockK
    private lateinit var errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate

    @MockK
    private lateinit var savedStateHandle: SavedStateHandle

    @RelaxedMockK
    private lateinit var baseErrorEventResolver: BaseErrorEventResolver

    @MockK
    private lateinit var primerSettings: PrimerSettings

    @MockK
    private lateinit var errorMapper: ErrorMapper

    private lateinit var component: StripeAchUserDetailsComponent

    private fun initComponent(
        firstName: String? = "John",
        lastName: String? = "Doe",
        emailAddress: String? = "john@doe.com"
    ) {
        every { savedStateHandle.get<String>("first_name") } returns firstName
        every { savedStateHandle.set<String>("first_name", any()) } just Runs
        every { savedStateHandle.get<String>("last_name") } returns lastName
        every { savedStateHandle.set<String>("last_name", any()) } just Runs
        every { savedStateHandle.get<String>("email_address") } returns emailAddress
        every { savedStateHandle.set<String>("email_address", any()) } just Runs

        component = StripeAchUserDetailsComponent(
            getClientSessionCustomerDetailsDelegate = getClientSessionCustomerDetailsDelegate,
            stripeAchClientSessionPatchDelegate = stripeAchClientSessionPatchDelegate,
            stripeAchTokenizationDelegate = stripeAchTokenizationDelegate,
            eventLoggingDelegate = eventLoggingDelegate,
            errorLoggingDelegate = errorLoggingDelegate,
            errorEventResolver = baseErrorEventResolver,
            savedStateHandle = savedStateHandle,
            primerSettings = primerSettings,
            errorMapper = errorMapper
        )
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(
            getClientSessionCustomerDetailsDelegate,
            stripeAchClientSessionPatchDelegate,
            stripeAchTokenizationDelegate,
            eventLoggingDelegate,
            errorLoggingDelegate,
            errorMapper
        )
    }

    @Test
    fun `start() should log event and emit UserDetailsRetrieved step if delegate call succeeds`() = runTest {
        initComponent()
        val details = GetClientSessionCustomerDetailsDelegate.ClientSessionCustomerDetails(
            firstName = "John",
            lastName = "Doe",
            emailAddress = "john@doe.com"
        )
        mockkObject(FirstNameValidator, LastNameValidator, EmailAddressValidator)
        every { FirstNameValidator.validate(any()) } returns null
        every { LastNameValidator.validate(any()) } returns null
        every { EmailAddressValidator.validate(any()) } returns null
        coEvery { getClientSessionCustomerDetailsDelegate.invoke() } returns Result.success(details)
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses =
            mutableListOf<PrimerValidationStatus<AchUserDetailsCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<AchUserDetailsStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        component.start()
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        coVerify {
            getClientSessionCustomerDetailsDelegate.invoke()
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName =
                StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_START_METHOD,
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name
            )
        }
        coVerify(exactly = 3) {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_COLLECTED_DATA_METHOD,
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name
            )
        }
        verify {
            FirstNameValidator.validate("John")
            LastNameValidator.validate("Doe")
            EmailAddressValidator.validate("john@doe.com")
        }
        assertEquals(emptyList(), errors)
        assertEquals(
            listOf(
                PrimerValidationStatus.Validating(
                    collectableData = AchUserDetailsCollectableData.FirstName(value = "John")
                ),
                PrimerValidationStatus.Validating(
                    collectableData = AchUserDetailsCollectableData.LastName(value = "Doe")
                ),
                PrimerValidationStatus.Validating(
                    collectableData = AchUserDetailsCollectableData.EmailAddress(value = "john@doe.com")
                ),
                PrimerValidationStatus.Valid(
                    collectableData = AchUserDetailsCollectableData.FirstName(value = "John")
                ),
                PrimerValidationStatus.Valid(
                    collectableData = AchUserDetailsCollectableData.LastName(value = "Doe")
                ),
                PrimerValidationStatus.Valid(
                    collectableData = AchUserDetailsCollectableData.EmailAddress(value = "john@doe.com")
                )
            ),
            validationStatuses
        )
        assertEquals(
            listOf<AchUserDetailsStep>(
                AchUserDetailsStep.UserDetailsRetrieved(
                    firstName = "John",
                    lastName = "Doe",
                    emailAddress = "john@doe.com"
                )
            ),
            steps
        )
        unmockkObject(FirstNameValidator, LastNameValidator, EmailAddressValidator)
    }

    @Test
    fun `start() should log to analytics and emit error if delegate call fails and in HEADLESS mode`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        initComponent()
        val error = Exception()
        coEvery { getClientSessionCustomerDetailsDelegate.invoke() } returns Result.failure(error)
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Runs
        val primerError = mockk<PrimerError>()
        coEvery { errorMapper.getPrimerError(any()) } returns primerError
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses =
            mutableListOf<PrimerValidationStatus<AchUserDetailsCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<AchUserDetailsStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        component.start()
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        verify(exactly = 0) {
            baseErrorEventResolver.resolve(any(), any())
        }
        coVerify {
            getClientSessionCustomerDetailsDelegate.invoke()
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName =
                StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_START_METHOD,
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name
            )
            errorMapper.getPrimerError(error)
            errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
        }
        assertEquals(listOf(primerError), errors)
        assertEquals(emptyList(), validationStatuses)
        assertEquals(emptyList(), steps)
    }

    @Test
    fun `start() should log to analytics, emit error and resolve checkout errors if delegate call fails and in DROP-in mode`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.DROP_IN
        initComponent()
        val error = Exception()
        coEvery { getClientSessionCustomerDetailsDelegate.invoke() } returns Result.failure(error)
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Runs
        val primerError = mockk<PrimerError>()
        coEvery { errorMapper.getPrimerError(any()) } returns primerError
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses =
            mutableListOf<PrimerValidationStatus<AchUserDetailsCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<AchUserDetailsStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        component.start()
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        verify(exactly = 1) {
            baseErrorEventResolver.resolve(error, ErrorMapperType.STRIPE)
        }
        coVerify {
            getClientSessionCustomerDetailsDelegate.invoke()
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName =
                StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_START_METHOD,
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name
            )
            errorMapper.getPrimerError(error)
            errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
        }
        assertEquals(listOf(primerError), errors)
        assertEquals(emptyList(), validationStatuses)
        assertEquals(emptyList(), steps)
    }

    @Test
    fun `updateCollectedData() should log event and emit valid status when FirstNameValidator validate() returns null`() = runTest {
        initComponent()
        mockkObject(FirstNameValidator)
        every { FirstNameValidator.validate(any()) } returns null
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses =
            mutableListOf<PrimerValidationStatus<AchUserDetailsCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<AchUserDetailsStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        val collectableData = AchUserDetailsCollectableData.FirstName(value = "John")
        component.updateCollectedData(collectableData)
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        coVerify {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName =
                StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_COLLECTED_DATA_METHOD,
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name
            )
        }
        verify {
            FirstNameValidator.validate(value = collectableData.value)
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
    }

    @Test
    fun `updateCollectedData() should log event and emit valid status when LastNameValidator validate() returns null`() = runTest {
        initComponent()
        mockkObject(LastNameValidator)
        every { LastNameValidator.validate(any()) } returns null
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses =
            mutableListOf<PrimerValidationStatus<AchUserDetailsCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<AchUserDetailsStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        val collectableData = AchUserDetailsCollectableData.LastName(value = "Doe")
        component.updateCollectedData(collectableData)
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        coVerify {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName =
                StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_COLLECTED_DATA_METHOD,
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name
            )
        }
        verify {
            LastNameValidator.validate(value = collectableData.value)
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
    }

    @Test
    fun `updateCollectedData() should log event and emit invalid status when FirstNameValidator validate() returns error`() = runTest {
        initComponent()
        mockkObject(FirstNameValidator)
        val validationError = mockk<PrimerValidationError>()
        every { FirstNameValidator.validate(any()) } returns validationError
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses =
            mutableListOf<PrimerValidationStatus<AchUserDetailsCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<AchUserDetailsStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        val collectableData = AchUserDetailsCollectableData.FirstName(value = " ")
        component.updateCollectedData(collectableData)
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        coVerify {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName =
                StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_COLLECTED_DATA_METHOD,
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name
            )
        }
        verify {
            FirstNameValidator.validate(value = " ")
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
    }

    @Test
    fun `updateCollectedData() should log event and emit invalid status when LastNameValidator validate() returns error`() = runTest {
        initComponent()
        mockkObject(LastNameValidator)
        val validationError = mockk<PrimerValidationError>()
        every { LastNameValidator.validate(any()) } returns validationError
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses =
            mutableListOf<PrimerValidationStatus<AchUserDetailsCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<AchUserDetailsStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        val collectableData = AchUserDetailsCollectableData.LastName(value = " ")
        component.updateCollectedData(collectableData)
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        coVerify {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName =
                StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_COLLECTED_DATA_METHOD,
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name
            )
        }
        verify {
            LastNameValidator.validate(value = " ")
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
    }

    @Test
    fun `updateCollectedData() should log event and emit invalid status when EmailAddressValidator validate() returns error`() = runTest {
        initComponent()
        mockkObject(EmailAddressValidator)
        val validationError = mockk<PrimerValidationError>()
        every { EmailAddressValidator.validate(any()) } returns validationError
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses =
            mutableListOf<PrimerValidationStatus<AchUserDetailsCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<AchUserDetailsStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        val collectableData = AchUserDetailsCollectableData.EmailAddress("john@doe.com")
        component.updateCollectedData(collectableData)
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        coVerify {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName =
                StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_COLLECTED_DATA_METHOD,
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name
            )
        }
        verify {
            EmailAddressValidator.validate(emailAddress = collectableData.value)
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
    }

    @Test
    fun `submit() should log event, patch client session, tokenize and emit UserDetailsCollected when patching delegate calls succeed`() = runTest {
        initComponent()
        coEvery {
            stripeAchClientSessionPatchDelegate.invoke(
                firstName = any(),
                lastName = any(),
                emailAddress = any()
            )
        } returns Result.success(Unit)
        coEvery { stripeAchTokenizationDelegate.invoke() } returns Result.success(Unit)
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses =
            mutableListOf<PrimerValidationStatus<AchUserDetailsCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<AchUserDetailsStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        component.submit()
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        coVerify {
            stripeAchClientSessionPatchDelegate(
                firstName = "John",
                lastName = "Doe",
                emailAddress = "john@doe.com"
            )
            stripeAchTokenizationDelegate.invoke()
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName =
                StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name
            )
        }
        assertEquals(emptyList(), errors)
        assertEquals(emptyList(), validationStatuses)
        assertEquals(
            listOf<AchUserDetailsStep>(AchUserDetailsStep.UserDetailsCollected),
            steps
        )
    }

    @Test
    fun `submit() should log event, patch client session, and emit error if tokenization delegate call fails`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        initComponent()
        val error = Exception()
        coEvery {
            stripeAchClientSessionPatchDelegate.invoke(
                firstName = any(),
                lastName = any(),
                emailAddress = any()
            )
        } returns Result.success(Unit)
        coEvery { stripeAchTokenizationDelegate.invoke() } returns Result.failure(error)
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Runs
        val primerError = mockk<PrimerError>()
        coEvery { errorMapper.getPrimerError(any()) } returns primerError
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses =
            mutableListOf<PrimerValidationStatus<AchUserDetailsCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<AchUserDetailsStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        component.submit()
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        verify(exactly = 0) {
            baseErrorEventResolver.resolve(any(), any())
        }
        coVerify {
            stripeAchClientSessionPatchDelegate(
                firstName = "John",
                lastName = "Doe",
                emailAddress = "john@doe.com"
            )
            stripeAchTokenizationDelegate.invoke()
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName =
                StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name
            )
            errorMapper.getPrimerError(error)
            errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
        }
        assertEquals(listOf(primerError), errors)
        assertEquals(emptyList(), validationStatuses)
        assertEquals(
            listOf<AchUserDetailsStep>(AchUserDetailsStep.UserDetailsCollected),
            steps
        )
    }

    @Test
    fun `submit() should log event, not tokenize, and emit error if client session patch delegate call fails`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        initComponent()
        val error = Exception()
        coEvery {
            stripeAchClientSessionPatchDelegate.invoke(
                firstName = any(),
                lastName = any(),
                emailAddress = any()
            )
        } returns Result.failure(error)
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Runs
        val primerError = mockk<PrimerError>()
        coEvery { errorMapper.getPrimerError(any()) } returns primerError
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses =
            mutableListOf<PrimerValidationStatus<AchUserDetailsCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<AchUserDetailsStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        component.submit()
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        verify(exactly = 0) {
            baseErrorEventResolver.resolve(any(), any())
        }
        coVerify {
            stripeAchClientSessionPatchDelegate(
                firstName = "John",
                lastName = "Doe",
                emailAddress = "john@doe.com"
            )
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName =
                StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name
            )
            errorMapper.getPrimerError(error)
            errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
        }
        assertEquals(listOf(primerError), errors)
        assertEquals(emptyList(), validationStatuses)
        assertEquals(emptyList(), steps)
    }

    @Test
    fun `submit() should log event, not patch client session, not tokenize, and emit error if first name is missing`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        initComponent(firstName = null)
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Runs
        val primerError = mockk<PrimerError>()
        coEvery { errorMapper.getPrimerError(any()) } returns primerError
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses =
            mutableListOf<PrimerValidationStatus<AchUserDetailsCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<AchUserDetailsStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        component.submit()
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        verify(exactly = 0) {
            baseErrorEventResolver.resolve(any(), any())
        }
        coVerify {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName =
                StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name
            )
            errorMapper.getPrimerError(any())
            errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
        }
        assertEquals(listOf(primerError), errors)
        assertEquals(emptyList(), validationStatuses)
        assertEquals(emptyList(), steps)
    }

    @Test
    fun `submit() should log event, not patch client session, not tokenize, and emit error if last name is missing`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        initComponent(lastName = null)
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Runs
        val primerError = mockk<PrimerError>()
        coEvery { errorMapper.getPrimerError(any()) } returns primerError
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses =
            mutableListOf<PrimerValidationStatus<AchUserDetailsCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<AchUserDetailsStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        component.submit()
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        verify(exactly = 0) {
            baseErrorEventResolver.resolve(any(), any())
        }
        coVerify {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName =
                StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name
            )
            errorMapper.getPrimerError(any())
            errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
        }
        assertEquals(listOf(primerError), errors)
        assertEquals(emptyList(), validationStatuses)
        assertEquals(emptyList(), steps)
    }

    @Test
    fun `submit() should log event, not patch client session, not tokenize, and emit error if email address is missing`() = runTest {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        initComponent(emailAddress = null)
        coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
        coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } just Runs
        val primerError = mockk<PrimerError>()
        coEvery { errorMapper.getPrimerError(any()) } returns primerError
        val errors = mutableListOf<PrimerError>()
        val errorJob = component.componentError.collectIn(errors, this)
        val validationStatuses =
            mutableListOf<PrimerValidationStatus<AchUserDetailsCollectableData>>()
        val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
        val steps = mutableListOf<AchUserDetailsStep>()
        val stepJob = component.componentStep.collectIn(steps, this)

        component.submit()
        delay(1.seconds)
        errorJob.cancel()
        validationJob.cancel()
        stepJob.cancel()

        verify(exactly = 0) {
            baseErrorEventResolver.resolve(any(), any())
        }
        coVerify {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName =
                StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name
            )
            errorMapper.getPrimerError(any())
            errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
        }
        assertEquals(listOf(primerError), errors)
        assertEquals(emptyList(), validationStatuses)
        assertEquals(emptyList(), steps)
    }
}
