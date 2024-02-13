package io.primer.android.components.manager.nolPay.payment

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.components.data.payments.paymentMethods.nolpay.error.NolPayErrorMapper
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayPaymentDataValidatorRegistry
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.components.manager.nolPay.analytics.NolPayAnalyticsConstants
import io.primer.android.components.manager.nolPay.payment.component.NolPayPaymentComponent
import io.primer.android.components.manager.nolPay.payment.composable.NolPayPaymentCollectableData
import io.primer.android.components.manager.nolPay.payment.composable.NolPayPaymentStep
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.base.DefaultHeadlessManagerDelegate
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayStartPaymentDelegate
import io.primer.android.test.extensions.toListDuring
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.nolpay.api.exceptions.NolPaySdkException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class NolPayPaymentComponentTest {

    @RelaxedMockK
    lateinit var startPaymentDelegate: NolPayStartPaymentDelegate

    @RelaxedMockK
    lateinit var headlessManagerDelegate: DefaultHeadlessManagerDelegate

    @RelaxedMockK
    lateinit var dataValidatorRegistry: NolPayPaymentDataValidatorRegistry

    @RelaxedMockK
    lateinit var eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate

    @RelaxedMockK
    lateinit var errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate

    @RelaxedMockK
    lateinit var errorMapper: NolPayErrorMapper

    private lateinit var component: NolPayPaymentComponent

    @BeforeEach
    fun setUp() {
        component = NolPayPaymentComponent(
            startPaymentDelegate,
            headlessManagerDelegate,
            eventLoggingDelegate,
            errorLoggingDelegate,
            dataValidatorRegistry,
            errorMapper
        )
    }

    @Test
    fun `start should log correct sdk analytics event`() {
        coEvery { startPaymentDelegate.start() }.returns(Result.success(Unit))

        runTest {
            component.start()
        }

        coVerify {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                PaymentMethodType.NOL_PAY.name,
                NolPayAnalyticsConstants.PAYMENT_START_METHOD,
                hashMapOf()
            )
        }
    }

    @Test
    fun `start should emit CollectStartPaymentData step when NolPayStartPaymentDelegate start was successful`() {
        coEvery { startPaymentDelegate.start() }.returns(Result.success(Unit))

        runTest {
            component.start()
            assertEquals(
                NolPayPaymentStep.CollectCardAndPhoneData,
                component.componentStep.first()
            )
        }

        coVerify { startPaymentDelegate.start() }
    }

    @Test
    fun `start should emit PrimerError when NolPayStartPaymentDelegate start failed`() {
        val exception = mockk<NolPaySdkException>(relaxed = true)
        coEvery { startPaymentDelegate.start() }.returns(Result.failure(exception))

        runTest {
            component.start()
            assertNotNull(component.componentError.first())
        }

        coVerify { startPaymentDelegate.start() }
        coVerify { errorMapper.getPrimerError(exception) }
        coVerify {
            errorLoggingDelegate.logSdkAnalyticsErrors(
                errorMapper.getPrimerError(exception)
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
                NolPayAnalyticsConstants.PAYMENT_UPDATE_COLLECTED_DATA_METHOD
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
                    PrimerValidationStatus.Valid(collectableData)
                ),
                validationStatuses
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
                    PrimerValidationStatus.Invalid(listOf(validationError), collectableData)
                ),
                validationStatuses
            )
        }

        coVerify { dataValidatorRegistry.getValidator(any()).validate(any()) }
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
                        errorMapper.getPrimerError(exception),
                        collectableData
                    )
                ),
                validationStatuses
            )
        }

        coVerify { dataValidatorRegistry.getValidator(any()).validate(any()) }
    }

    @Test
    fun `submit should log correct sdk analytics event`() {
        val paymentStep = mockk<NolPayPaymentStep>(relaxed = true)
        coEvery {
            startPaymentDelegate.handleCollectedCardData(
                any()
            )
        }.returns(Result.success(paymentStep))

        runTest {
            component.submit()
        }

        coVerify {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                PaymentMethodType.NOL_PAY.name,
                NolPayAnalyticsConstants.PAYMENT_SUBMIT_DATA_METHOD,
                hashMapOf()
            )
        }
    }

    @Test
    fun `submit should emit next step when NolPayPaymentDataValidatorRegistry handleCollectedCardData was successful`() {
        val paymentStep = mockk<NolPayPaymentStep>(relaxed = true)
        coEvery {
            startPaymentDelegate.handleCollectedCardData(
                any()
            )
        }.returns(Result.success(paymentStep))
        runTest {
            component.submit()
        }

        coVerify { startPaymentDelegate.handleCollectedCardData(any()) }
        coVerify(exactly = 0) { errorMapper.getPrimerError(any()) }
        coVerify(exactly = 0) {
            errorLoggingDelegate.logSdkAnalyticsErrors(
                errorMapper.getPrimerError(any())
            )
        }
    }

    @Test
    fun `submit should emit PrimerError when NolPayStartPaymentDelegate handleCollectedCardData failed`() {
        val exception = mockk<NolPaySdkException>(relaxed = true)
        coEvery {
            startPaymentDelegate.handleCollectedCardData(
                any()
            )
        }.returns(Result.failure(exception))
        runTest {
            component.submit()
            assertNotNull(component.componentError.first())
        }

        coVerify { startPaymentDelegate.handleCollectedCardData(any()) }
        coVerify { errorMapper.getPrimerError(exception) }
        coVerify {
            errorLoggingDelegate.logSdkAnalyticsErrors(
                errorMapper.getPrimerError(exception)
            )
        }
    }
}
