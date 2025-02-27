package io.primer.android.nolpay.api.manager.linkCard.component

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.nolpay.api.manager.analytics.NolPayAnalyticsConstants
import io.primer.android.nolpay.api.manager.linkCard.composable.NolPayLinkCardStep
import io.primer.android.nolpay.api.manager.linkCard.composable.NolPayLinkCollectableData
import io.primer.android.nolpay.implementation.linkCard.domain.validation.NolPayLinkDataValidatorRegistry
import io.primer.android.nolpay.implementation.linkCard.presentation.NolPayLinkPaymentCardDelegate
import io.primer.android.core.toListDuring
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsValidationErrorLoggingDelegate
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.nolpay.api.exceptions.NolPaySdkException
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
internal class NolPayLinkCardComponentTest {
    @RelaxedMockK
    lateinit var linkPaymentCardDelegate: NolPayLinkPaymentCardDelegate

    @RelaxedMockK
    lateinit var dataValidatorRegistry: NolPayLinkDataValidatorRegistry

    @RelaxedMockK
    lateinit var eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate

    @RelaxedMockK
    lateinit var errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate

    @RelaxedMockK
    private lateinit var validationErrorLoggingDelegate: SdkAnalyticsValidationErrorLoggingDelegate

    @RelaxedMockK
    lateinit var errorMapperRegistry: ErrorMapperRegistry

    @RelaxedMockK
    lateinit var savedStateHandle: SavedStateHandle

    private lateinit var component: NolPayLinkCardComponent

    @BeforeEach
    fun setUp() {
        component =
            NolPayLinkCardComponent(
                linkPaymentCardDelegate = linkPaymentCardDelegate,
                validatorRegistry = dataValidatorRegistry,
                errorLoggingDelegate = errorLoggingDelegate,
                eventLoggingDelegate = eventLoggingDelegate,
                validationErrorLoggingDelegate = validationErrorLoggingDelegate,
                errorMapperRegistry = errorMapperRegistry,
                savedStateHandle = savedStateHandle,
            )
    }

    @Test
    fun `start should log correct sdk analytics event`() {
        coEvery { linkPaymentCardDelegate.start() }.returns(Result.success(Unit))

        runTest {
            component.start()
        }

        coVerify {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                PaymentMethodType.NOL_PAY.name,
                NolPayAnalyticsConstants.LINK_CARD_START_METHOD,
                hashMapOf(),
            )
        }
    }

    @Test
    fun `start should emit CollectTagData step when NolPayLinkPaymentCardDelegate start was successful`() {
        coEvery { linkPaymentCardDelegate.start() }.returns(Result.success(Unit))

        runTest {
            component.start()
            assertEquals(NolPayLinkCardStep.CollectTagData, component.componentStep.first())
        }

        coVerify { linkPaymentCardDelegate.start() }
    }

    @Test
    fun `start should emit PrimerError when NolPayLinkPaymentCardDelegate start failed`() {
        val exception = mockk<NolPaySdkException>(relaxed = true)
        coEvery { linkPaymentCardDelegate.start() }.returns(Result.failure(exception))

        runTest {
            component.start()
            assertNotNull(component.componentError.first())
        }

        coVerify { linkPaymentCardDelegate.start() }
        coVerify { errorMapperRegistry.getPrimerError(exception) }
        coVerify {
            errorLoggingDelegate.logSdkAnalyticsErrors(
                errorMapperRegistry.getPrimerError(exception),
            )
        }
    }

    @Test
    fun `updateCollectedData should log correct sdk analytics event`() {
        val collectableData = mockk<NolPayLinkCollectableData>(relaxed = true)
        coEvery { dataValidatorRegistry.getValidator(any()).validate(any()) }
            .returns(Result.success(listOf()))

        runTest {
            component.updateCollectedData(collectableData)
        }

        coVerify {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                PaymentMethodType.NOL_PAY.name,
                NolPayAnalyticsConstants.LINK_CARD_UPDATE_COLLECTED_DATA_METHOD,
            )
        }
    }

    @Test
    fun `updateCollectedData should emit validation statuses with successful validation when NolPayLinkDataValidatorRegistry validate returned no errors`() {
        val collectableData = mockk<NolPayLinkCollectableData>(relaxed = true)
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
    fun `updateCollectedData should emit validation statuses with validation errors when NolPayLinkDataValidatorRegistry validate returned errors`() {
        val collectableData = mockk<NolPayLinkCollectableData>(relaxed = true)
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
    fun `updateCollectedData should emit validation statuses error when NolPayLinkDataValidatorRegistry validate failed`() {
        val collectableData = mockk<NolPayLinkCollectableData>(relaxed = true)
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
        val step = mockk<NolPayLinkCardStep>(relaxed = true)
        coEvery {
            linkPaymentCardDelegate.handleCollectedCardData(
                any(),
                any(),
            )
        }.returns(Result.success(step))

        runTest {
            component.submit()
        }

        coVerify {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                PaymentMethodType.NOL_PAY.name,
                NolPayAnalyticsConstants.LINK_CARD_SUBMIT_DATA_METHOD,
                hashMapOf(),
            )
        }
    }

    @Test
    fun `submit should emit next step when NolPayLinkPaymentCardDelegate handleCollectedCardData was successful`() {
        val step = mockk<NolPayLinkCardStep>(relaxed = true)
        coEvery {
            linkPaymentCardDelegate.handleCollectedCardData(
                any(),
                any(),
            )
        }.returns(Result.success(step))

        runTest {
            component.submit()
            assertEquals(step, component.componentStep.first())
        }

        coVerify { linkPaymentCardDelegate.handleCollectedCardData(any(), any()) }
        coVerify(exactly = 0) { errorMapperRegistry.getPrimerError(any()) }
        coVerify(exactly = 0) {
            errorLoggingDelegate.logSdkAnalyticsErrors(
                errorMapperRegistry.getPrimerError(any()),
            )
        }
    }

    @Test
    fun `submit should emit PrimerError when NolPayLinkPaymentCardDelegate handleCollectedCardData failed`() {
        val exception = mockk<NolPaySdkException>(relaxed = true)
        coEvery {
            linkPaymentCardDelegate.handleCollectedCardData(
                any(),
                any(),
            )
        }.returns(Result.failure(exception))

        runTest {
            component.submit()
            assertNotNull(component.componentError.first())
        }

        coVerify { linkPaymentCardDelegate.handleCollectedCardData(any(), any()) }
        coVerify { errorMapperRegistry.getPrimerError(exception) }
        coVerify {
            errorLoggingDelegate.logSdkAnalyticsErrors(
                errorMapperRegistry.getPrimerError(exception),
            )
        }
    }
}
