package io.primer.android.stripe.ach.api.component

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
import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.core.extensions.collectIn
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsValidationErrorLoggingDelegate
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.helpers.ManualFlowSuccessHandler
import io.primer.android.payments.core.resume.domain.handler.PendingResumeHandler
import io.primer.android.stripe.ach.InstantExecutorExtension
import io.primer.android.stripe.ach.api.additionalInfo.AchAdditionalInfo
import io.primer.android.stripe.ach.api.component.StripeAchUserDetailsComponent.CheckoutFailureException
import io.primer.android.stripe.ach.api.composable.AchUserDetailsCollectableData
import io.primer.android.stripe.ach.api.composable.AchUserDetailsStep
import io.primer.android.stripe.ach.implementation.analytics.StripeAchUserDetailsAnalyticsConstants
import io.primer.android.stripe.ach.implementation.payment.presentation.StripeAchPaymentDelegate
import io.primer.android.stripe.ach.implementation.payment.resume.handler.StripeAchDecision
import io.primer.android.stripe.ach.implementation.selection.presentation.StripeAchBankFlowDelegate
import io.primer.android.stripe.ach.implementation.session.data.validation.validator.EmailAddressValidator
import io.primer.android.stripe.ach.implementation.session.data.validation.validator.FirstNameValidator
import io.primer.android.stripe.ach.implementation.session.data.validation.validator.LastNameValidator
import io.primer.android.stripe.ach.implementation.session.presentation.GetClientSessionCustomerDetailsDelegate
import io.primer.android.stripe.ach.implementation.session.presentation.StripeAchClientSessionPatchDelegate
import io.primer.android.stripe.ach.implementation.tokenization.presentation.StripeAchTokenizationDelegate
import io.primer.android.stripe.ach.implementation.tokenization.presentation.model.StripeAchTokenizationInputable
import kotlinx.coroutines.CancellationException
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
    private lateinit var stripeAchPaymentDelegate: StripeAchPaymentDelegate

    @MockK
    private lateinit var eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate

    @MockK
    private lateinit var stripeAchBankFlowDelegate: StripeAchBankFlowDelegate

    @MockK
    private lateinit var errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate

    @RelaxedMockK
    private lateinit var validationErrorLoggingDelegate: SdkAnalyticsValidationErrorLoggingDelegate

    @MockK
    private lateinit var savedStateHandle: SavedStateHandle

    @RelaxedMockK
    private lateinit var errorMapperRegistry: ErrorMapperRegistry

    @MockK
    private lateinit var successHandler: CheckoutSuccessHandler

    @MockK
    private lateinit var pendingResumeHandler: PendingResumeHandler

    @MockK
    private lateinit var manualFlowSuccessHandler: ManualFlowSuccessHandler

    @MockK
    private lateinit var config: PrimerConfig

    @MockK
    private lateinit var primerSettings: PrimerSettings

    private lateinit var component: StripeAchUserDetailsComponent

    private fun initComponent(
        firstName: String? = "John",
        lastName: String? = "Doe",
        emailAddress: String? = "john@doe.com",
        hadFirstSubmission: Boolean = true,
    ) {
        every { savedStateHandle.get<String>("first_name") } returns firstName
        every { savedStateHandle.set<String>("first_name", any()) } just Runs
        every { savedStateHandle.get<String>("last_name") } returns lastName
        every { savedStateHandle.set<String>("last_name", any()) } just Runs
        every { savedStateHandle.get<String>("email_address") } returns emailAddress
        every { savedStateHandle.set<String>("email_address", any()) } just Runs
        every { savedStateHandle.get<Boolean>("had_first_submission") } returns hadFirstSubmission
        every { savedStateHandle.set<String>("had_first_submission", any()) } just Runs

        component =
            StripeAchUserDetailsComponent(
                getClientSessionCustomerDetailsDelegate = getClientSessionCustomerDetailsDelegate,
                stripeAchClientSessionPatchDelegate = stripeAchClientSessionPatchDelegate,
                stripeAchTokenizationDelegate = stripeAchTokenizationDelegate,
                stripeAchPaymentDelegate = stripeAchPaymentDelegate,
                eventLoggingDelegate = eventLoggingDelegate,
                stripeAchBankFlowDelegate = stripeAchBankFlowDelegate,
                errorLoggingDelegate = errorLoggingDelegate,
                errorMapperRegistry = errorMapperRegistry,
                validationErrorLoggingDelegate = validationErrorLoggingDelegate,
                successHandler = successHandler,
                pendingResumeHandler = pendingResumeHandler,
                manualFlowSuccessHandler = manualFlowSuccessHandler,
                savedStateHandle = savedStateHandle,
                config = config,
                primerSettings = primerSettings,
            )
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(
            getClientSessionCustomerDetailsDelegate,
            stripeAchClientSessionPatchDelegate,
            stripeAchTokenizationDelegate,
            stripeAchPaymentDelegate,
            eventLoggingDelegate,
            stripeAchBankFlowDelegate,
            errorLoggingDelegate,
            errorMapperRegistry,
            validationErrorLoggingDelegate,
            successHandler,
        )
    }

    @Test
    fun `start() should log event and emit UserDetailsRetrieved step if delegate call succeeds`() =
        runTest {
            initComponent()
            val details =
                GetClientSessionCustomerDetailsDelegate.ClientSessionCustomerDetails(
                    firstName = "John",
                    lastName = "Doe",
                    emailAddress = "john@doe.com",
                )
            mockkObject(
                FirstNameValidator,
                LastNameValidator,
                EmailAddressValidator,
            )
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
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
            }
            verify(exactly = 0) {
                FirstNameValidator.validate(any())
                LastNameValidator.validate(any())
                EmailAddressValidator.validate(any())
            }
            assertEquals(emptyList(), errors)
            assertEquals(emptyList(), validationStatuses)
            assertEquals(
                listOf<AchUserDetailsStep>(
                    AchUserDetailsStep.UserDetailsRetrieved(
                        firstName = "John",
                        lastName = "Doe",
                        emailAddress = "john@doe.com",
                    ),
                ),
                steps,
            )
            unmockkObject(
                FirstNameValidator,
                LastNameValidator,
                EmailAddressValidator,
            )
        }

    @Test
    fun `start() should log to analytics and emit error if delegate call fails and in HEADLESS mode`() =
        runTest {
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            initComponent()
            val error = Exception()
            coEvery { getClientSessionCustomerDetailsDelegate.invoke() } returns Result.failure(error)
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } returns Result.success(Unit)
            val primerError = mockk<PrimerError>()
            coEvery { errorMapperRegistry.getPrimerError(any()) } returns primerError
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

            coVerify(exactly = 0) {
                stripeAchPaymentDelegate.handleError(any())
            }
            coVerify {
                getClientSessionCustomerDetailsDelegate.invoke()
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName =
                        StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_START_METHOD,
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
                errorMapperRegistry.getPrimerError(error)
                errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
            }
            assertEquals(listOf(primerError), errors)
            assertEquals(emptyList(), validationStatuses)
            assertEquals(emptyList(), steps)
        }

    @Test
    fun `start() should log to analytics, emit error and resolve checkout errors if delegate call fails and in DROP-in mode`() =
        runTest {
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.DROP_IN
            initComponent()
            val error = Exception()
            coEvery { getClientSessionCustomerDetailsDelegate.invoke() } returns Result.failure(error)
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            coEvery { stripeAchPaymentDelegate.handleError(any()) } just Runs
            coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } returns Result.success(Unit)
            val primerError = mockk<PrimerError>()
            coEvery { errorMapperRegistry.getPrimerError(any()) } returns primerError
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

            coVerify(exactly = 1) {
                stripeAchPaymentDelegate.handleError(error)
            }
            coVerify {
                getClientSessionCustomerDetailsDelegate.invoke()
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName =
                        StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_START_METHOD,
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
                errorMapperRegistry.getPrimerError(error)
                errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
            }
            assertEquals(listOf(primerError), errors)
            assertEquals(emptyList(), validationStatuses)
            assertEquals(emptyList(), steps)
        }

    @Test
    fun `updateCollectedData() should log event and emit valid status when FirstNameValidator validate() returns null`() =
        runTest {
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
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
            }
            verify {
                FirstNameValidator.validate(value = collectableData.value)
            }
            assertEquals(emptyList(), errors)
            assertEquals(
                listOf(
                    PrimerValidationStatus.Validating(collectableData),
                    PrimerValidationStatus.Valid(collectableData),
                ),
                validationStatuses,
            )
            assertEquals(emptyList(), steps)
            unmockkObject(FirstNameValidator)
        }

    @Test
    fun `updateCollectedData() should log event and emit valid status when LastNameValidator validate() returns null`() =
        runTest {
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
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
            }
            verify {
                LastNameValidator.validate(value = collectableData.value)
            }
            assertEquals(emptyList(), errors)
            assertEquals(
                listOf(
                    PrimerValidationStatus.Validating(collectableData),
                    PrimerValidationStatus.Valid(collectableData),
                ),
                validationStatuses,
            )
            assertEquals(emptyList(), steps)
            unmockkObject(LastNameValidator)
        }

    @Test
    fun `updateCollectedData() should log event and emit invalid status when FirstNameValidator validate() returns error`() =
        runTest {
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
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
                validationErrorLoggingDelegate.logSdkAnalyticsError(validationError)
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
                        collectableData = collectableData,
                    ),
                ),
                validationStatuses,
            )
            assertEquals(emptyList(), steps)
            unmockkObject(FirstNameValidator)
        }

    @Test
    fun `updateCollectedData() should log event and emit invalid status when LastNameValidator validate() returns error`() =
        runTest {
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
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
                validationErrorLoggingDelegate.logSdkAnalyticsError(validationError)
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
                        collectableData = collectableData,
                    ),
                ),
                validationStatuses,
            )
            assertEquals(emptyList(), steps)
            unmockkObject(LastNameValidator)
        }

    @Test
    fun `updateCollectedData() should log event and emit invalid status when EmailAddressValidator validate() returns error`() =
        runTest {
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
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
                validationErrorLoggingDelegate.logSdkAnalyticsError(validationError)
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
                        collectableData = collectableData,
                    ),
                ),
                validationStatuses,
            )
            assertEquals(emptyList(), steps)
            unmockkObject(EmailAddressValidator)
        }

    @Test
    fun `submit() should log event and return early when submit() is called for the first time and first name is invalid`() =
        runTest {
            initComponent(firstName = "", hadFirstSubmission = false)
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            val errors = mutableListOf<PrimerError>()
            val errorJob = component.componentError.collectIn(errors, this)
            val validationStatuses =
                mutableListOf<PrimerValidationStatus<AchUserDetailsCollectableData>>()
            val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
            val steps = mutableListOf<AchUserDetailsStep>()
            val stepJob = component.componentStep.collectIn(steps, this)
            mockkObject(FirstNameValidator)
            val validationError = mockk<PrimerValidationError>()
            every { FirstNameValidator.validate(any()) } returns validationError

            component.submit()
            delay(1.seconds)
            errorJob.cancel()
            validationJob.cancel()
            stepJob.cancel()

            coVerify(exactly = 0) {
                stripeAchClientSessionPatchDelegate(
                    firstName = any(),
                    lastName = any(),
                    emailAddress = any(),
                )
                stripeAchTokenizationDelegate.tokenize(any())
            }
            coVerify {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName =
                        StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
                validationErrorLoggingDelegate.logSdkAnalyticsError(validationError)
            }
            coVerify(exactly = 3) {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName =
                        StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_COLLECTED_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
            }
            assertEquals(emptyList(), errors)
            assertEquals(
                listOf(
                    PrimerValidationStatus.Validating(
                        collectableData = AchUserDetailsCollectableData.FirstName(value = ""),
                    ),
                    PrimerValidationStatus.Invalid(
                        validationError = validationError,
                        collectableData = AchUserDetailsCollectableData.FirstName(value = ""),
                    ),
                    PrimerValidationStatus.Validating(
                        collectableData = AchUserDetailsCollectableData.LastName(value = "Doe"),
                    ),
                    PrimerValidationStatus.Valid(
                        collectableData = AchUserDetailsCollectableData.LastName(value = "Doe"),
                    ),
                    PrimerValidationStatus.Validating(
                        collectableData = AchUserDetailsCollectableData.EmailAddress(value = "john@doe.com"),
                    ),
                    PrimerValidationStatus.Valid(
                        collectableData = AchUserDetailsCollectableData.EmailAddress(value = "john@doe.com"),
                    ),
                ),
                validationStatuses,
            )
            assertEquals(emptyList(), steps)
            unmockkObject(FirstNameValidator)
        }

    @Test
    fun `submit() should log event and return early when submit() is called for the first time and last name is invalid`() =
        runTest {
            initComponent(lastName = "", hadFirstSubmission = false)
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            val errors = mutableListOf<PrimerError>()
            val errorJob = component.componentError.collectIn(errors, this)
            val validationStatuses =
                mutableListOf<PrimerValidationStatus<AchUserDetailsCollectableData>>()
            val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
            val steps = mutableListOf<AchUserDetailsStep>()
            val stepJob = component.componentStep.collectIn(steps, this)
            mockkObject(LastNameValidator)
            val validationError = mockk<PrimerValidationError>()
            every { LastNameValidator.validate(any()) } returns validationError

            component.submit()
            delay(1.seconds)
            errorJob.cancel()
            validationJob.cancel()
            stepJob.cancel()

            coVerify(exactly = 0) {
                stripeAchClientSessionPatchDelegate(
                    firstName = any(),
                    lastName = any(),
                    emailAddress = any(),
                )
                stripeAchTokenizationDelegate.tokenize(any())
            }
            coVerify {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName =
                        StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
                validationErrorLoggingDelegate.logSdkAnalyticsError(validationError)
            }
            coVerify(exactly = 3) {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName =
                        StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_COLLECTED_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
            }
            assertEquals(emptyList(), errors)
            assertEquals(
                listOf(
                    PrimerValidationStatus.Validating(
                        collectableData = AchUserDetailsCollectableData.FirstName(value = "John"),
                    ),
                    PrimerValidationStatus.Valid(
                        collectableData = AchUserDetailsCollectableData.FirstName(value = "John"),
                    ),
                    PrimerValidationStatus.Validating(
                        collectableData = AchUserDetailsCollectableData.LastName(value = ""),
                    ),
                    PrimerValidationStatus.Invalid(
                        validationError = validationError,
                        collectableData = AchUserDetailsCollectableData.LastName(value = ""),
                    ),
                    PrimerValidationStatus.Validating(
                        collectableData = AchUserDetailsCollectableData.EmailAddress(value = "john@doe.com"),
                    ),
                    PrimerValidationStatus.Valid(
                        collectableData = AchUserDetailsCollectableData.EmailAddress(value = "john@doe.com"),
                    ),
                ),
                validationStatuses,
            )
            assertEquals(emptyList(), steps)
            unmockkObject(LastNameValidator)
        }

    @Test
    fun `submit() should log event and return early when submit() is called for the first time and email address is invalid`() =
        runTest {
            initComponent(emailAddress = "", hadFirstSubmission = false)
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            val errors = mutableListOf<PrimerError>()
            val errorJob = component.componentError.collectIn(errors, this)
            val validationStatuses =
                mutableListOf<PrimerValidationStatus<AchUserDetailsCollectableData>>()
            val validationJob = component.componentValidationStatus.collectIn(validationStatuses, this)
            val steps = mutableListOf<AchUserDetailsStep>()
            val stepJob = component.componentStep.collectIn(steps, this)
            mockkObject(EmailAddressValidator)
            val validationError = mockk<PrimerValidationError>()
            every { EmailAddressValidator.validate(any()) } returns validationError

            component.submit()
            delay(1.seconds)
            errorJob.cancel()
            validationJob.cancel()
            stepJob.cancel()

            coVerify(exactly = 0) {
                stripeAchClientSessionPatchDelegate(
                    firstName = any(),
                    lastName = any(),
                    emailAddress = any(),
                )
                stripeAchTokenizationDelegate.tokenize(any())
            }
            coVerify {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName =
                        StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
                validationErrorLoggingDelegate.logSdkAnalyticsError(validationError)
            }
            coVerify(exactly = 3) {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName =
                        StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_COLLECTED_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
            }
            assertEquals(emptyList(), errors)
            assertEquals(
                listOf(
                    PrimerValidationStatus.Validating(
                        collectableData = AchUserDetailsCollectableData.FirstName(value = "John"),
                    ),
                    PrimerValidationStatus.Valid(
                        collectableData = AchUserDetailsCollectableData.FirstName(value = "John"),
                    ),
                    PrimerValidationStatus.Validating(
                        collectableData = AchUserDetailsCollectableData.LastName(value = "Doe"),
                    ),
                    PrimerValidationStatus.Valid(
                        collectableData = AchUserDetailsCollectableData.LastName(value = "Doe"),
                    ),
                    PrimerValidationStatus.Validating(
                        collectableData = AchUserDetailsCollectableData.EmailAddress(value = ""),
                    ),
                    PrimerValidationStatus.Invalid(
                        validationError = validationError,
                        collectableData = AchUserDetailsCollectableData.EmailAddress(value = ""),
                    ),
                ),
                validationStatuses,
            )
            assertEquals(emptyList(), steps)
            unmockkObject(EmailAddressValidator)
        }

    @Test
    fun `submit() should log event, patch client session, tokenize, call success handler and emit UserDetailsCollected when patching delegate calls succeed and in AUTO mode`() =
        runTest {
            initComponent()
            every { config.settings.paymentHandling } returns PrimerPaymentHandling.AUTO
            coEvery {
                stripeAchClientSessionPatchDelegate.invoke(
                    firstName = any(),
                    lastName = any(),
                    emailAddress = any(),
                )
            } returns Result.success(Unit)
            val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>()
            coEvery { stripeAchTokenizationDelegate.tokenize(any()) } returns Result.success(paymentMethodTokenData)
            coEvery { stripeAchPaymentDelegate.handlePaymentMethodToken(any(), any()) } returns
                Result.success(
                    PaymentDecision.Pending("", null),
                )
            val stripeAchDecision =
                StripeAchDecision(
                    sdkCompleteUrl = "sdkCompleteUrl",
                    stripePaymentIntentId = "stripePaymentIntentId",
                    stripeClientSecret = "stripeClientSecret",
                )
            coEvery { stripeAchPaymentDelegate.lastDecision } returns stripeAchDecision
            val payment = mockk<Payment>()
            coEvery { stripeAchBankFlowDelegate.handle(any(), any(), any()) } returns
                Result.success(
                    StripeAchBankFlowDelegate.StripeAchBankFlowResult(
                        payment = payment,
                        mandateTimestamp = "timestamp",
                    ),
                )
            coEvery { successHandler.handle(any(), any()) } returns Unit
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
                    emailAddress = "john@doe.com",
                )
                stripeAchTokenizationDelegate.tokenize(
                    StripeAchTokenizationInputable(
                        paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                        primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                    ),
                )
                stripeAchPaymentDelegate.handlePaymentMethodToken(
                    paymentMethodTokenData = paymentMethodTokenData,
                    primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                )
                stripeAchPaymentDelegate.lastDecision
                stripeAchBankFlowDelegate.handle(
                    clientSecret = stripeAchDecision.stripeClientSecret,
                    paymentIntentId = stripeAchDecision.stripePaymentIntentId,
                    sdkCompleteUrl = stripeAchDecision.sdkCompleteUrl,
                )
                successHandler.handle(payment = payment, additionalInfo = null)
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName =
                        StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
            }
            assertEquals(emptyList(), errors)
            assertEquals(emptyList(), validationStatuses)
            assertEquals(
                listOf<AchUserDetailsStep>(AchUserDetailsStep.UserDetailsCollected),
                steps,
            )
        }

    @Test
    fun `submit() should log event, patch client session, tokenize, call pending resume handler, manual success handler and emit UserDetailsCollected when patching delegate calls succeed and in MANUAL mode`() =
        runTest {
            initComponent()
            every { config.settings.paymentHandling } returns PrimerPaymentHandling.MANUAL
            coEvery {
                stripeAchClientSessionPatchDelegate.invoke(
                    firstName = any(),
                    lastName = any(),
                    emailAddress = any(),
                )
            } returns Result.success(Unit)
            val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>()
            coEvery { stripeAchTokenizationDelegate.tokenize(any()) } returns Result.success(paymentMethodTokenData)
            coEvery { stripeAchPaymentDelegate.handlePaymentMethodToken(any(), any()) } returns
                Result.success(
                    PaymentDecision.Pending("", null),
                )
            val stripeAchDecision =
                StripeAchDecision(
                    sdkCompleteUrl = "sdkCompleteUrl",
                    stripePaymentIntentId = "stripePaymentIntentId",
                    stripeClientSecret = "stripeClientSecret",
                )
            coEvery { stripeAchPaymentDelegate.lastDecision } returns stripeAchDecision
            coEvery { stripeAchBankFlowDelegate.handle(any(), any(), any()) } returns
                Result.success(
                    StripeAchBankFlowDelegate.StripeAchBankFlowResult(payment = null, mandateTimestamp = "timestamp"),
                )
            coEvery { pendingResumeHandler.handle(any()) } returns Unit
            coEvery { manualFlowSuccessHandler.handle() } returns Unit
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
                    emailAddress = "john@doe.com",
                )
                stripeAchTokenizationDelegate.tokenize(
                    StripeAchTokenizationInputable(
                        paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                        primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                    ),
                )
                stripeAchPaymentDelegate.handlePaymentMethodToken(
                    paymentMethodTokenData = paymentMethodTokenData,
                    primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                )
                stripeAchPaymentDelegate.lastDecision
                stripeAchBankFlowDelegate.handle(
                    clientSecret = stripeAchDecision.stripeClientSecret,
                    paymentIntentId = stripeAchDecision.stripePaymentIntentId,
                    sdkCompleteUrl = stripeAchDecision.sdkCompleteUrl,
                )
                pendingResumeHandler.handle(additionalInfo = AchAdditionalInfo.MandateAccepted("timestamp"))
                manualFlowSuccessHandler.handle()
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName =
                        StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
            }
            assertEquals(emptyList(), errors)
            assertEquals(emptyList(), validationStatuses)
            assertEquals(
                listOf<AchUserDetailsStep>(AchUserDetailsStep.UserDetailsCollected),
                steps,
            )
        }

    @Test
    fun `submit() should log event, patch client session, emit UserDetailsCollected, and emit error if tokenization delegate call fails`() =
        runTest {
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            initComponent()
            val error = Exception()
            coEvery {
                stripeAchClientSessionPatchDelegate.invoke(
                    firstName = any(),
                    lastName = any(),
                    emailAddress = any(),
                )
            } returns Result.success(Unit)
            coEvery { stripeAchTokenizationDelegate.tokenize(any()) } returns Result.failure(error)
            coEvery { stripeAchPaymentDelegate.handleError(any()) } just Runs
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } returns Result.success(Unit)
            val primerError = mockk<PrimerError>()
            coEvery { errorMapperRegistry.getPrimerError(any()) } returns primerError
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

            coVerify(exactly = 0) {
                stripeAchPaymentDelegate.handlePaymentMethodToken(
                    paymentMethodTokenData = any(),
                    primerSessionIntent = any(),
                )
                stripeAchPaymentDelegate.lastDecision
                stripeAchBankFlowDelegate.handle(
                    clientSecret = any(),
                    paymentIntentId = any(),
                    sdkCompleteUrl = any(),
                )
                successHandler.handle(payment = any(), additionalInfo = any())
            }
            coVerify {
                stripeAchClientSessionPatchDelegate(
                    firstName = "John",
                    lastName = "Doe",
                    emailAddress = "john@doe.com",
                )
                stripeAchTokenizationDelegate.tokenize(
                    StripeAchTokenizationInputable(
                        paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                        primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                    ),
                )
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName =
                        StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
                errorMapperRegistry.getPrimerError(error)
                stripeAchPaymentDelegate.handleError(any<CheckoutFailureException>())
                errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
            }
            assertEquals(listOf(primerError), errors)
            assertEquals(emptyList(), validationStatuses)
            assertEquals(
                listOf<AchUserDetailsStep>(AchUserDetailsStep.UserDetailsCollected),
                steps,
            )
        }

    @Test
    fun `submit() should log event, patch client session, emit UserDetailsCollected, tokenize and emit error if payment delegate call fails`() =
        runTest {
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            initComponent()
            val error = Exception()
            coEvery {
                stripeAchClientSessionPatchDelegate.invoke(
                    firstName = any(),
                    lastName = any(),
                    emailAddress = any(),
                )
            } returns Result.success(Unit)
            val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>()
            coEvery { stripeAchTokenizationDelegate.tokenize(any()) } returns Result.success(paymentMethodTokenData)
            coEvery { stripeAchPaymentDelegate.handlePaymentMethodToken(any(), any()) } returns Result.failure(error)
            coEvery { stripeAchPaymentDelegate.handleError(any()) } just Runs
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } returns Result.success(Unit)
            val primerError = mockk<PrimerError>()
            coEvery { errorMapperRegistry.getPrimerError(any()) } returns primerError
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

            coVerify(exactly = 0) {
                stripeAchPaymentDelegate.lastDecision
                stripeAchBankFlowDelegate.handle(
                    clientSecret = any(),
                    paymentIntentId = any(),
                    sdkCompleteUrl = any(),
                )
                successHandler.handle(payment = any(), additionalInfo = any())
            }
            coVerify {
                stripeAchClientSessionPatchDelegate(
                    firstName = "John",
                    lastName = "Doe",
                    emailAddress = "john@doe.com",
                )
                stripeAchTokenizationDelegate.tokenize(
                    StripeAchTokenizationInputable(
                        paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                        primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                    ),
                )
                stripeAchPaymentDelegate.handlePaymentMethodToken(
                    paymentMethodTokenData = paymentMethodTokenData,
                    primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                )
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName =
                        StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
                errorMapperRegistry.getPrimerError(error)
                stripeAchPaymentDelegate.handleError(any<CheckoutFailureException>())
                errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
            }
            assertEquals(listOf(primerError), errors)
            assertEquals(emptyList(), validationStatuses)
            assertEquals(
                listOf<AchUserDetailsStep>(AchUserDetailsStep.UserDetailsCollected),
                steps,
            )
        }

    @Test
    fun `submit() should log event, patch client session, emit UserDetailsCollected, tokenize, create payment, call error handler and emit error if bank flow delegate call fails with non-cancellation error`() =
        runTest {
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            initComponent()
            val error = Exception()
            coEvery {
                stripeAchClientSessionPatchDelegate.invoke(
                    firstName = any(),
                    lastName = any(),
                    emailAddress = any(),
                )
            } returns Result.success(Unit)
            val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>()
            coEvery { stripeAchTokenizationDelegate.tokenize(any()) } returns Result.success(paymentMethodTokenData)
            coEvery { stripeAchPaymentDelegate.handlePaymentMethodToken(any(), any()) } returns
                Result.success(
                    PaymentDecision.Pending("", null),
                )
            coEvery { stripeAchPaymentDelegate.handleError(any()) } just Runs
            val stripeAchDecision =
                StripeAchDecision(
                    sdkCompleteUrl = "sdkCompleteUrl",
                    stripePaymentIntentId = "stripePaymentIntentId",
                    stripeClientSecret = "stripeClientSecret",
                )
            coEvery { stripeAchPaymentDelegate.lastDecision } returns stripeAchDecision
            coEvery { stripeAchBankFlowDelegate.handle(any(), any(), any()) } returns Result.failure(error)
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } returns Result.success(Unit)
            val primerError = mockk<PrimerError>()
            coEvery { errorMapperRegistry.getPrimerError(any()) } returns primerError
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

            coVerify(exactly = 0) {
                successHandler.handle(payment = any(), additionalInfo = any())
            }
            coVerify {
                stripeAchClientSessionPatchDelegate(
                    firstName = "John",
                    lastName = "Doe",
                    emailAddress = "john@doe.com",
                )
                stripeAchTokenizationDelegate.tokenize(
                    StripeAchTokenizationInputable(
                        paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                        primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                    ),
                )
                stripeAchPaymentDelegate.lastDecision
                stripeAchPaymentDelegate.handlePaymentMethodToken(
                    paymentMethodTokenData = paymentMethodTokenData,
                    primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                )
                stripeAchBankFlowDelegate.handle(
                    clientSecret = stripeAchDecision.stripeClientSecret,
                    paymentIntentId = stripeAchDecision.stripePaymentIntentId,
                    sdkCompleteUrl = stripeAchDecision.sdkCompleteUrl,
                )
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName =
                        StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
                errorMapperRegistry.getPrimerError(error)
                stripeAchPaymentDelegate.handleError(any<CheckoutFailureException>())
                errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
            }
            assertEquals(listOf(primerError), errors)
            assertEquals(emptyList(), validationStatuses)
            assertEquals(
                listOf<AchUserDetailsStep>(AchUserDetailsStep.UserDetailsCollected),
                steps,
            )
        }

    @Test
    fun `submit() should log event, patch client session, emit UserDetailsCollected, tokenize, create payment, call error handler and emit error if bank flow delegate call fails with PaymentMethodCancelledException`() =
        runTest {
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            initComponent()
            val error = PaymentMethodCancelledException("STRIPE_ACH")
            coEvery {
                stripeAchClientSessionPatchDelegate.invoke(
                    firstName = any(),
                    lastName = any(),
                    emailAddress = any(),
                )
            } returns Result.success(Unit)
            val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>()
            coEvery { stripeAchTokenizationDelegate.tokenize(any()) } returns Result.success(paymentMethodTokenData)
            coEvery { stripeAchPaymentDelegate.handlePaymentMethodToken(any(), any()) } returns
                Result.success(
                    PaymentDecision.Pending("", null),
                )
            coEvery { stripeAchPaymentDelegate.handleError(any()) } just Runs
            val stripeAchDecision =
                StripeAchDecision(
                    sdkCompleteUrl = "sdkCompleteUrl",
                    stripePaymentIntentId = "stripePaymentIntentId",
                    stripeClientSecret = "stripeClientSecret",
                )
            coEvery { stripeAchPaymentDelegate.lastDecision } returns stripeAchDecision
            coEvery { stripeAchBankFlowDelegate.handle(any(), any(), any()) } returns Result.failure(error)
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } returns Result.success(Unit)
            val primerError = mockk<PrimerError>()
            coEvery { errorMapperRegistry.getPrimerError(any()) } returns primerError
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

            coVerify(exactly = 0) {
                successHandler.handle(payment = any(), additionalInfo = any())
            }
            coVerify {
                stripeAchClientSessionPatchDelegate(
                    firstName = "John",
                    lastName = "Doe",
                    emailAddress = "john@doe.com",
                )
                stripeAchTokenizationDelegate.tokenize(
                    StripeAchTokenizationInputable(
                        paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                        primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                    ),
                )
                stripeAchPaymentDelegate.lastDecision
                stripeAchPaymentDelegate.handlePaymentMethodToken(
                    paymentMethodTokenData = paymentMethodTokenData,
                    primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                )
                stripeAchBankFlowDelegate.handle(
                    clientSecret = stripeAchDecision.stripeClientSecret,
                    paymentIntentId = stripeAchDecision.stripePaymentIntentId,
                    sdkCompleteUrl = stripeAchDecision.sdkCompleteUrl,
                )
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName =
                        StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
                errorMapperRegistry.getPrimerError(error)
                stripeAchPaymentDelegate.handleError(any<CheckoutFailureException>())
                errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
            }
            assertEquals(listOf(primerError), errors)
            assertEquals(emptyList(), validationStatuses)
            assertEquals(
                listOf<AchUserDetailsStep>(AchUserDetailsStep.UserDetailsCollected),
                steps,
            )
        }

    @Test
    fun `submit() should log event, patch client session, emit UserDetailsCollected, tokenize, create payment and swallow error if bank flow delegate call fails with CancellationException`() =
        runTest {
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            initComponent()
            val error = CancellationException()
            coEvery {
                stripeAchClientSessionPatchDelegate.invoke(
                    firstName = any(),
                    lastName = any(),
                    emailAddress = any(),
                )
            } returns Result.success(Unit)
            val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>()
            coEvery { stripeAchTokenizationDelegate.tokenize(any()) } returns Result.success(paymentMethodTokenData)
            coEvery { stripeAchPaymentDelegate.handlePaymentMethodToken(any(), any()) } returns
                Result.success(
                    PaymentDecision.Pending("", null),
                )
            val stripeAchDecision =
                StripeAchDecision(
                    sdkCompleteUrl = "sdkCompleteUrl",
                    stripePaymentIntentId = "stripePaymentIntentId",
                    stripeClientSecret = "stripeClientSecret",
                )
            coEvery { stripeAchPaymentDelegate.lastDecision } returns stripeAchDecision
            coEvery { stripeAchBankFlowDelegate.handle(any(), any(), any()) } returns Result.failure(error)
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

            coVerify(exactly = 0) {
                successHandler.handle(payment = any(), additionalInfo = any())
                stripeAchPaymentDelegate.handleError(any())
                errorMapperRegistry.getPrimerError(any())
                errorLoggingDelegate.logSdkAnalyticsErrors(any())
            }
            coVerify {
                stripeAchClientSessionPatchDelegate(
                    firstName = "John",
                    lastName = "Doe",
                    emailAddress = "john@doe.com",
                )
                stripeAchTokenizationDelegate.tokenize(
                    StripeAchTokenizationInputable(
                        paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                        primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                    ),
                )
                stripeAchPaymentDelegate.lastDecision
                stripeAchPaymentDelegate.handlePaymentMethodToken(
                    paymentMethodTokenData = paymentMethodTokenData,
                    primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                )
                stripeAchBankFlowDelegate.handle(
                    clientSecret = stripeAchDecision.stripeClientSecret,
                    paymentIntentId = stripeAchDecision.stripePaymentIntentId,
                    sdkCompleteUrl = stripeAchDecision.sdkCompleteUrl,
                )
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName =
                        StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
            }
            assertEquals(emptyList(), validationStatuses)
            assertEquals(
                listOf<AchUserDetailsStep>(AchUserDetailsStep.UserDetailsCollected),
                steps,
            )
        }

    @Test
    fun `submit() should log event, not tokenize, not create payment and call error handler emit error if client session patching fails`() =
        runTest {
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            initComponent()
            val error = Exception()
            coEvery {
                stripeAchClientSessionPatchDelegate.invoke(
                    firstName = any(),
                    lastName = any(),
                    emailAddress = any(),
                )
            } returns Result.failure(error)
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            coEvery { stripeAchPaymentDelegate.handleError(any()) } just Runs
            coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } returns Result.success(Unit)
            val primerError = mockk<PrimerError>()
            coEvery { errorMapperRegistry.getPrimerError(any()) } returns primerError
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

            coVerify(exactly = 0) {
                stripeAchTokenizationDelegate.tokenize(any())
                stripeAchPaymentDelegate.handlePaymentMethodToken(
                    paymentMethodTokenData = any(),
                    primerSessionIntent = any(),
                )
                stripeAchPaymentDelegate.lastDecision
                stripeAchBankFlowDelegate.handle(
                    clientSecret = any(),
                    paymentIntentId = any(),
                    sdkCompleteUrl = any(),
                )
                successHandler.handle(payment = any(), additionalInfo = any())
            }
            coVerify {
                stripeAchClientSessionPatchDelegate(
                    firstName = "John",
                    lastName = "Doe",
                    emailAddress = "john@doe.com",
                )
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName =
                        StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
                stripeAchPaymentDelegate.handleError(error)
                errorMapperRegistry.getPrimerError(error)
                errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
            }
            assertEquals(listOf(primerError), errors)
            assertEquals(emptyList(), validationStatuses)
            assertEquals(emptyList(), steps)
        }

    @Test
    fun `submit() should log event, not patch client session, not tokenize, and emit error if first name is missing`() =
        runTest {
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            initComponent(firstName = null)
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } returns Result.success(Unit)
            val primerError = mockk<PrimerError>()
            coEvery { errorMapperRegistry.getPrimerError(any()) } returns primerError
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

            coVerify(exactly = 0) {
                stripeAchPaymentDelegate.handleError(any())
            }
            coVerify {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName =
                        StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
                errorMapperRegistry.getPrimerError(any())
                errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
            }
            assertEquals(listOf(primerError), errors)
            assertEquals(emptyList(), validationStatuses)
            assertEquals(emptyList(), steps)
        }

    @Test
    fun `submit() should log event, not patch client session, not tokenize, and emit error if last name is missing`() =
        runTest {
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            initComponent(lastName = null)
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } returns Result.success(Unit)
            val primerError = mockk<PrimerError>()
            coEvery { errorMapperRegistry.getPrimerError(any()) } returns primerError
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

            coVerify(exactly = 0) {
                stripeAchPaymentDelegate.handleError(any())
            }
            coVerify {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName =
                        StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
                errorMapperRegistry.getPrimerError(any())
                errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
            }
            assertEquals(listOf(primerError), errors)
            assertEquals(emptyList(), validationStatuses)
            assertEquals(emptyList(), steps)
        }

    @Test
    fun `submit() should log event, not patch client session, not tokenize, and emit error if email address is missing`() =
        runTest {
            every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
            initComponent(emailAddress = null)
            coEvery { eventLoggingDelegate.logSdkAnalyticsEvent(any(), any()) } just Runs
            coEvery { errorLoggingDelegate.logSdkAnalyticsErrors(any()) } returns Result.success(Unit)
            val primerError = mockk<PrimerError>()
            coEvery { errorMapperRegistry.getPrimerError(any()) } returns primerError
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

            coVerify(exactly = 0) {
                stripeAchPaymentDelegate.handleError(any())
            }
            coVerify {
                eventLoggingDelegate.logSdkAnalyticsEvent(
                    methodName =
                        StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                )
                errorMapperRegistry.getPrimerError(any())
                errorLoggingDelegate.logSdkAnalyticsErrors(primerError)
            }
            assertEquals(listOf(primerError), errors)
            assertEquals(emptyList(), validationStatuses)
            assertEquals(emptyList(), steps)
        }
}
