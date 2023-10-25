package io.primer.android.components.manager.nolPay.linkCard.component

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.ExperimentalPrimerApi
import io.primer.android.InstantExecutorExtension
import io.primer.android.components.data.payments.paymentMethods.nolpay.error.NolPayErrorMapper
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayLinkDataValidatorRegistry
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.components.manager.nolPay.analytics.NolPayAnalyticsConstants
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCardStep
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCollectableData
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayLinkPaymentCardDelegate
import io.primer.android.extensions.toListDuring
import io.primer.nolpay.api.exceptions.NolPaySdkException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalPrimerApi::class)
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class NolPayLinkCardComponentTest {

    @RelaxedMockK
    lateinit var linkPaymentCardDelegate: NolPayLinkPaymentCardDelegate

    @RelaxedMockK
    lateinit var dataValidatorRegistry: NolPayLinkDataValidatorRegistry

    @RelaxedMockK
    lateinit var errorMapper: NolPayErrorMapper

    @RelaxedMockK
    lateinit var savedStateHandle: SavedStateHandle

    private lateinit var component: NolPayLinkCardComponent

    @BeforeEach
    fun setUp() {
        component = NolPayLinkCardComponent(
            linkPaymentCardDelegate,
            dataValidatorRegistry,
            errorMapper,
            savedStateHandle
        )
    }

    @Test
    fun `start should log correct sdk analytics event`() {
        runTest {
            component.start()
        }

        coVerify {
            linkPaymentCardDelegate.logSdkAnalyticsEvent(
                NolPayAnalyticsConstants.LINK_CARD_START_METHOD,
                hashMapOf()
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
        coVerify { errorMapper.getPrimerError(exception) }
        coVerify {
            linkPaymentCardDelegate.logSdkAnalyticsErrors(
                errorMapper.getPrimerError(exception)
            )
        }
    }

    @Test
    fun `updateCollectedData should log correct sdk analytics event`() {
        val collectableData = mockk<NolPayLinkCollectableData>(relaxed = true)
        runTest {
            component.updateCollectedData(collectableData)
        }

        coVerify {
            linkPaymentCardDelegate.logSdkAnalyticsEvent(
                NolPayAnalyticsConstants.LINK_CARD_UPDATE_COLLECTED_DATA_METHOD,
                hashMapOf(
                    NolPayAnalyticsConstants.COLLECTED_DATA_SDK_PARAMS to collectableData.toString()
                )
            )
        }
    }

    @Test
    fun `updateCollectedData should emit validation statuses with validation errors when NolPayLinkDataValidatorRegistry validate was successful`() {
        val collectableData = mockk<NolPayLinkCollectableData>(relaxed = true)
        coEvery { dataValidatorRegistry.getValidator(any()).validate(any()) }
            .returns(Result.success(listOf()))

        runTest {
            component.updateCollectedData(collectableData)
            val validationStatuses = component.componentValidationStatus.toListDuring(0.5.seconds)
            assertEquals(
                listOf(
                    PrimerValidationStatus.Validating(collectableData),
                    PrimerValidationStatus.Validated(emptyList(), collectableData)
                ),
                validationStatuses
            )
        }

        coVerify { dataValidatorRegistry.getValidator(any()).validate(any()) }
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
        runTest {
            component.submit()
        }

        coVerify {
            linkPaymentCardDelegate.logSdkAnalyticsEvent(
                NolPayAnalyticsConstants.LINK_CARD_SUBMIT_DATA_METHOD,
                hashMapOf()
            )
        }
    }

    @Test
    fun `submit should emit next step when NolPayLinkPaymentCardDelegate handleCollectedCardData was successful`() {
        val step = mockk<NolPayLinkCardStep>(relaxed = true)
        coEvery {
            linkPaymentCardDelegate.handleCollectedCardData(
                any(),
                any()
            )
        }.returns(Result.success(step))

        runTest {
            component.submit()
            assertEquals(step, component.componentStep.first())
        }

        coVerify { linkPaymentCardDelegate.handleCollectedCardData(any(), any()) }
        coVerify(exactly = 0) { errorMapper.getPrimerError(any()) }
        coVerify(exactly = 0) {
            linkPaymentCardDelegate.logSdkAnalyticsErrors(
                errorMapper.getPrimerError(
                    any()
                )
            )
        }
    }

    @Test
    fun `submit should emit PrimerError when NolPayLinkPaymentCardDelegate handleCollectedCardData failed`() {
        val exception = mockk<NolPaySdkException>(relaxed = true)
        coEvery {
            linkPaymentCardDelegate.handleCollectedCardData(
                any(),
                any()
            )
        }.returns(Result.failure(exception))

        runTest {
            component.submit()
            assertNotNull(component.componentError.first())
        }

        coVerify { linkPaymentCardDelegate.handleCollectedCardData(any(), any()) }
        coVerify { errorMapper.getPrimerError(exception) }
        coVerify {
            linkPaymentCardDelegate.logSdkAnalyticsErrors(
                errorMapper.getPrimerError(
                    exception
                )
            )
        }
    }
}
