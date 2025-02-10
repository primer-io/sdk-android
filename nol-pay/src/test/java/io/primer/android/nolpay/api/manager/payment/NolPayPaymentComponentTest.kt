package io.primer.android.nolpay.api.manager.payment

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.nolpay.api.manager.analytics.NolPayAnalyticsConstants
import io.primer.android.nolpay.api.manager.payment.component.NolPayPaymentComponent
import io.primer.android.nolpay.api.manager.payment.composable.NolPayPaymentCollectableData
import io.primer.android.nolpay.api.manager.payment.composable.NolPayPaymentStep
import io.primer.android.nolpay.implementation.common.presentation.BaseNolPayDelegate
import io.primer.android.nolpay.implementation.paymentCard.payment.delegate.NolPayPaymentDelegate
import io.primer.android.nolpay.implementation.paymentCard.tokenization.presentation.NolPayTokenizationDelegate
import io.primer.android.nolpay.implementation.validation.NolPayPaymentDataValidatorRegistry
import io.primer.android.core.toListDuring
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsValidationErrorLoggingDelegate
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.nolpay.api.exceptions.NolPaySdkException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class NolPayPaymentComponentTest {
    @RelaxedMockK
    lateinit var baseNolPayDelegate: BaseNolPayDelegate

    @RelaxedMockK
    lateinit var tokenizationDelegate: NolPayTokenizationDelegate

    @RelaxedMockK
    lateinit var paymentDelegate: NolPayPaymentDelegate

    @RelaxedMockK
    lateinit var eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate

    @RelaxedMockK
    lateinit var errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate

    @RelaxedMockK
    lateinit var validationErrorLoggingDelegate: SdkAnalyticsValidationErrorLoggingDelegate

    @RelaxedMockK
    lateinit var dataValidatorRegistry: NolPayPaymentDataValidatorRegistry

    @RelaxedMockK
    lateinit var errorMapperRegistry: ErrorMapperRegistry

    private lateinit var component: NolPayPaymentComponent

    @BeforeEach
    fun setUp() {
        component =
            NolPayPaymentComponent(
                baseNolPayDelegate = baseNolPayDelegate,
                tokenizationDelegate = tokenizationDelegate,
                paymentDelegate = paymentDelegate,
                eventLoggingDelegate = eventLoggingDelegate,
                errorLoggingDelegate = errorLoggingDelegate,
                validationErrorLoggingDelegate = validationErrorLoggingDelegate,
                validatorRegistry = dataValidatorRegistry,
                errorMapperRegistry = errorMapperRegistry,
            )

        coEvery { paymentDelegate.componentStep } returns MutableSharedFlow()
    }

    @Test
    fun `start should log correct sdk analytics event`() {
        coEvery { baseNolPayDelegate.start() }.returns(Result.success(Unit))

        runTest {
            component.start()
        }

        coVerify {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                PaymentMethodType.NOL_PAY.name,
                NolPayAnalyticsConstants.PAYMENT_START_METHOD,
                hashMapOf(),
            )
        }
    }

    @Test
    fun `start should emit CollectStartPaymentData step when NolPayStartPaymentDelegate start was successful`() {
        coEvery { baseNolPayDelegate.start() }.returns(Result.success(Unit))

        runTest {
            component.start()
            assertEquals(
                NolPayPaymentStep.CollectCardAndPhoneData,
                component.componentStep.first(),
            )
        }

        coVerify { baseNolPayDelegate.start() }
    }

    @Test
    fun `start should emit PrimerError when NolPayStartPaymentDelegate start failed`() {
        val exception = mockk<NolPaySdkException>(relaxed = true)
        coEvery { baseNolPayDelegate.start() }.returns(Result.failure(exception))

        runTest {
            component.start()
            assertNotNull(component.componentError.first())
        }

        coVerify { baseNolPayDelegate.start() }
        coVerify { errorMapperRegistry.getPrimerError(exception) }
        coVerify {
            errorLoggingDelegate.logSdkAnalyticsErrors(
                errorMapperRegistry.getPrimerError(exception),
            )
        }
    }

    @Test
    fun `updateCollectedData should log correct sdk analytics event`() {
        val collectableData = mockk<NolPayPaymentCollectableData>(relaxed = true)
        coEvery { dataValidatorRegistry.getValidator(any()).validate(any()) }
            .returns(Result.success(listOf()))

        runTest {
            component.updateCollectedData(collectableData)
        }

        coVerify {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                PaymentMethodType.NOL_PAY.name,
                NolPayAnalyticsConstants.PAYMENT_UPDATE_COLLECTED_DATA_METHOD,
            )
        }
    }

    @Test
    fun `updateCollectedData should emit validation statuses with successful validation when NolPayPaymentDataValidatorRegistry validate returned no errors`() {
        val collectableData = mockk<NolPayPaymentCollectableData>(relaxed = true)
        coEvery { dataValidatorRegistry.getValidator(any()).validate(any()) }
            .returns(Result.success(listOf()))

        runTest {
            component.updateCollectedData(collectableData)
            val validationStatuses = component.componentValidationStatus.toListDuring(0.5.seconds)
            assertEquals(
                listOf(
                    PrimerValidationStatus.Validating(collectableData),
                    PrimerValidationStatus.Valid(collectableData),
                ),
                validationStatuses,
            )
        }

        coVerify { dataValidatorRegistry.getValidator(any()).validate(any()) }
    }

    @Test
    fun `updateCollectedData should emit validation statuses with validation errors when NolPayPaymentDataValidatorRegistry validate returned errors`() {
        val collectableData = mockk<NolPayPaymentCollectableData>(relaxed = true)
        val validationError = mockk<PrimerValidationError>(relaxed = true)
        coEvery { dataValidatorRegistry.getValidator(any()).validate(any()) }
            .returns(Result.success(listOf(validationError)))
        runTest {
            component.updateCollectedData(collectableData)
            val validationStatuses = component.componentValidationStatus.toListDuring(0.5.seconds)
            assertEquals(
                listOf(
                    PrimerValidationStatus.Validating(collectableData),
                    PrimerValidationStatus.Invalid(listOf(validationError), collectableData),
                ),
                validationStatuses,
            )
        }

        coVerify {
            validationErrorLoggingDelegate.logSdkAnalyticsError(validationError)
            dataValidatorRegistry.getValidator(any()).validate(any())
        }
    }

    @Test
    fun `updateCollectedData should emit validation statuses error when NolPayPaymentDataValidatorRegistry validate failed`() {
        val collectableData = mockk<NolPayPaymentCollectableData>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)
        coEvery { dataValidatorRegistry.getValidator(any()).validate(any()) }
            .returns(Result.failure(exception))
        runTest {
            component.updateCollectedData(collectableData)
            val validationStatuses = component.componentValidationStatus.toListDuring(0.5.seconds)
            assertEquals(
                listOf(
                    PrimerValidationStatus.Validating(collectableData),
                    PrimerValidationStatus.Error(
                        errorMapperRegistry.getPrimerError(exception),
                        collectableData,
                    ),
                ),
                validationStatuses,
            )
        }

        coVerify {
            validationErrorLoggingDelegate.logSdkAnalyticsError(errorMapperRegistry.getPrimerError(exception))
            dataValidatorRegistry.getValidator(any()).validate(any())
        }
    }

    @Test
    fun `submit should log correct sdk analytics event`() {
        runTest {
            component.submit()
        }

        coVerify {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                PaymentMethodType.NOL_PAY.name,
                NolPayAnalyticsConstants.PAYMENT_SUBMIT_DATA_METHOD,
                hashMapOf(),
            )
        }
    }

    @Test
    fun `submit should emit success when handleCollectedCardData was successful`() {
        val collectableData = mockk<NolPayPaymentCollectableData.NolPayCardAndPhoneData>(relaxed = true)

        val paymentMethodToken = mockk<PrimerPaymentMethodTokenData>(relaxed = true)
        val paymentDecision = mockk<PaymentDecision.Pending>(relaxed = true)

        coEvery {
            dataValidatorRegistry.getValidator(
                collectableData,
            ).validate(
                collectableData,
            )
        } returns Result.success(emptyList<PrimerValidationError>())

        coEvery {
            tokenizationDelegate.tokenize(any())
        } returns Result.success(paymentMethodToken)

        coEvery {
            paymentDelegate.handlePaymentMethodToken(any(), any())
        } returns Result.success(paymentDecision)

        runTest {
            component.updateCollectedData(collectableData)
            component.submit()
        }

        coVerify { tokenizationDelegate.tokenize(any()) }
        coVerify { paymentDelegate.handlePaymentMethodToken(any(), any()) }
        coVerify(exactly = 0) { errorMapperRegistry.getPrimerError(any()) }
        coVerify(exactly = 0) {
            errorLoggingDelegate.logSdkAnalyticsErrors(
                errorMapperRegistry.getPrimerError(any()),
            )
        }
    }

    @Test
    fun `submit should not emit PrimerError when NolPayPaymentDelegate handlePaymentMethodToken failed`() {
        val exception = mockk<NolPaySdkException>(relaxed = true)
        val collectableData = mockk<NolPayPaymentCollectableData.NolPayCardAndPhoneData>(relaxed = true)

        val paymentMethodToken = mockk<PrimerPaymentMethodTokenData>(relaxed = true)

        coEvery {
            dataValidatorRegistry.getValidator(
                collectableData,
            ).validate(
                collectableData,
            )
        } returns Result.success(emptyList<PrimerValidationError>())

        coEvery {
            tokenizationDelegate.tokenize(any())
        } returns Result.success(paymentMethodToken)

        coEvery {
            paymentDelegate.handlePaymentMethodToken(any(), any())
        } returns Result.failure(exception)
        runTest {
            component.updateCollectedData(collectableData)
            component.submit()
        }

        coVerify { tokenizationDelegate.tokenize(any()) }
        coVerify { paymentDelegate.handlePaymentMethodToken(any(), any()) }
        coVerify { paymentDelegate.handleError(exception) }
        coVerify(exactly = 0) { errorMapperRegistry.getPrimerError(exception) }
        coVerify(exactly = 0) {
            errorLoggingDelegate.logSdkAnalyticsErrors(
                errorMapperRegistry.getPrimerError(exception),
            )
        }
    }

    @Test
    fun `submit should not emit PrimerError when NolPayTokenizationDelegate tokenize failed`() {
        val exception = mockk<NolPaySdkException>(relaxed = true)
        val collectableData = mockk<NolPayPaymentCollectableData.NolPayCardAndPhoneData>(relaxed = true)

        coEvery {
            dataValidatorRegistry.getValidator(
                collectableData,
            ).validate(
                collectableData,
            )
        } returns Result.success(emptyList<PrimerValidationError>())

        coEvery {
            tokenizationDelegate.tokenize(any())
        } returns Result.failure(exception)

        runTest {
            component.updateCollectedData(collectableData)
            component.submit()
        }

        coVerify { tokenizationDelegate.tokenize(any()) }
        coVerify { paymentDelegate.handleError(exception) }
        coVerify(exactly = 0) { errorMapperRegistry.getPrimerError(exception) }
        coVerify(exactly = 0) {
            errorLoggingDelegate.logSdkAnalyticsErrors(
                errorMapperRegistry.getPrimerError(exception),
            )
        }
    }

    @Test
    fun `submit should emit PrimerError when NolPayPaymentDelegate requestPayment failed`() {
        val exception = mockk<NolPaySdkException>(relaxed = true)
        val collectableData = mockk<NolPayPaymentCollectableData.NolPayTagData>(relaxed = true)

        coEvery {
            dataValidatorRegistry.getValidator(
                collectableData,
            ).validate(
                collectableData,
            )
        } returns Result.success(emptyList<PrimerValidationError>())

        coEvery {
            paymentDelegate.requestPayment(any())
        } returns Result.failure(exception)

        runTest {
            component.updateCollectedData(collectableData)
            component.submit()
        }

        coVerify { paymentDelegate.requestPayment(any()) }
        coVerify { errorMapperRegistry.getPrimerError(exception) }
        coVerify {
            errorLoggingDelegate.logSdkAnalyticsErrors(
                errorMapperRegistry.getPrimerError(exception),
            )
        }
    }

    @Test
    fun `submit should emit success when NolPayPaymentDelegate requestPayment is successful`() {
        val collectableData = mockk<NolPayPaymentCollectableData.NolPayTagData>(relaxed = true)

        coEvery {
            dataValidatorRegistry.getValidator(
                collectableData,
            ).validate(
                collectableData,
            )
        } returns Result.success(emptyList<PrimerValidationError>())

        coEvery {
            paymentDelegate.requestPayment(any())
        } returns Result.success(true)

        coEvery {
            paymentDelegate.completePayment()
        } returns Result.success(Unit)

        runTest {
            component.updateCollectedData(collectableData)
            component.submit()
        }

        coVerify { paymentDelegate.requestPayment(any()) }
    }
}
