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
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayUnlinkDataValidatorRegistry
import io.primer.android.components.manager.nolPay.analytics.NolPayAnalyticsConstants
import io.primer.android.components.manager.nolPay.unlinkCard.component.NolPayUnlinkCardComponent
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCardStep
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCollectableData
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayUnlinkPaymentCardDelegate
import io.primer.nolpay.api.exceptions.NolPaySdkException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalPrimerApi::class)
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class NolPayUnlinkCardComponentTest {

    @RelaxedMockK
    lateinit var unlinkPaymentCardDelegate: NolPayUnlinkPaymentCardDelegate

    @RelaxedMockK
    lateinit var dataValidatorRegistry: NolPayUnlinkDataValidatorRegistry

    @RelaxedMockK
    lateinit var errorMapper: NolPayErrorMapper

    @RelaxedMockK
    lateinit var savedStateHandle: SavedStateHandle

    private lateinit var component: NolPayUnlinkCardComponent

    @BeforeEach
    fun setUp() {
        component = NolPayUnlinkCardComponent(
            unlinkPaymentCardDelegate,
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
            unlinkPaymentCardDelegate.logSdkAnalyticsEvent(
                NolPayAnalyticsConstants.UNLINK_CARD_START_METHOD,
                hashMapOf()
            )
        }
    }

    @Test
    fun `start should emit CollectCardData step when NolPayUnlinkPaymentCardDelegate start was successful`() {
        coEvery { unlinkPaymentCardDelegate.start() }.returns(Result.success(Unit))
        runTest {
            component.start()
            assertEquals(
                NolPayUnlinkCardStep.CollectCardAndPhoneData,
                component.componentStep.first()
            )
        }

        coVerify { unlinkPaymentCardDelegate.start() }
    }

    @Test
    fun `start should emit PrimerError when NolPayUnlinkPaymentCardDelegate start failed`() {
        val exception = mockk<NolPaySdkException>(relaxed = true)
        coEvery { unlinkPaymentCardDelegate.start() }.returns(Result.failure(exception))
        runTest {
            component.start()
            assertNotNull(component.componentError.first())
        }

        coVerify { unlinkPaymentCardDelegate.start() }
        coVerify { errorMapper.getPrimerError(exception) }
        coVerify {
            unlinkPaymentCardDelegate.logSdkAnalyticsErrors(
                errorMapper.getPrimerError(
                    exception
                )
            )
        }
    }

    @Test
    fun `updateCollectedData should log correct sdk analytics event`() {
        val collectableData = mockk<NolPayUnlinkCollectableData>(relaxed = true)
        runTest {
            component.updateCollectedData(collectableData)
        }

        coVerify {
            unlinkPaymentCardDelegate.logSdkAnalyticsEvent(
                NolPayAnalyticsConstants.UNLINK_CARD_UPDATE_COLLECTED_DATA_METHOD,
                hashMapOf(
                    NolPayAnalyticsConstants.COLLECTED_DATA_SDK_PARAMS to collectableData.toString()
                )
            )
        }
    }

    @Test
    fun `updateCollectedData should emit validation errors when NolPayUnlinkDataValidatorRegistry validate was successful`() {
        val collectableData = mockk<NolPayUnlinkCollectableData>(relaxed = true)
        coEvery { dataValidatorRegistry.getValidator(any()).validate(any()) }.returns(listOf())
        runTest {
            component.updateCollectedData(collectableData)
            assertEquals(listOf(), component.componentValidationStatus.first())
        }

        coVerify { dataValidatorRegistry.getValidator(any()).validate(any()) }
    }

    @Test
    fun `submit should log correct sdk analytics event`() {
        runTest {
            component.submit()
        }

        coVerify {
            unlinkPaymentCardDelegate.logSdkAnalyticsEvent(
                NolPayAnalyticsConstants.UNLINK_CARD_SUBMIT_DATA_METHOD,
                hashMapOf()
            )
        }
    }

    @Test
    fun `submit should emit next step when NolPayUnlinkPaymentCardDelegate handleCollectedCardData was successful`() {
        val step = mockk<NolPayUnlinkCardStep>(relaxed = true)
        coEvery {
            unlinkPaymentCardDelegate.handleCollectedCardData(
                any(),
                any()
            )
        }.returns(Result.success(step))
        runTest {
            component.submit()
            assertEquals(step, component.componentStep.first())
        }

        coVerify { unlinkPaymentCardDelegate.handleCollectedCardData(any(), any()) }
        coVerify(exactly = 0) { errorMapper.getPrimerError(any()) }
        coVerify(exactly = 0) {
            unlinkPaymentCardDelegate.logSdkAnalyticsErrors(
                errorMapper.getPrimerError(
                    any()
                )
            )
        }
    }

    @Test
    fun `submit should emit PrimerError when NolPayUnlinkPaymentCardDelegate handleCollectedCardData failed`() {
        val exception = mockk<NolPaySdkException>(relaxed = true)
        coEvery {
            unlinkPaymentCardDelegate.handleCollectedCardData(
                any(),
                any()
            )
        }.returns(Result.failure(exception))
        runTest {
            component.submit()
            assertNotNull(component.componentError.first())
        }

        coVerify { unlinkPaymentCardDelegate.handleCollectedCardData(any(), any()) }
        coVerify { errorMapper.getPrimerError(exception) }
        coVerify {
            unlinkPaymentCardDelegate.logSdkAnalyticsErrors(
                errorMapper.getPrimerError(
                    exception
                )
            )
        }
    }
}
