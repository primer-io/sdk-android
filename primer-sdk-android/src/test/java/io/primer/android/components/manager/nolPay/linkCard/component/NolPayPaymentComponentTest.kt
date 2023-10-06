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
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayPaymentDataValidatorRegistry
import io.primer.android.components.manager.nolPay.analytics.NolPayAnalyticsConstants
import io.primer.android.components.manager.nolPay.payment.composable.NolPayPaymentCollectableData
import io.primer.android.components.manager.nolPay.payment.composable.NolPayPaymentStep
import io.primer.android.components.manager.nolPay.payment.component.NolPayPaymentComponent
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayStartPaymentDelegate
import io.primer.nolpay.api.exceptions.NolPaySdkException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalPrimerApi::class)
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class NolPayPaymentComponentTest {

    @RelaxedMockK
    lateinit var startPaymentDelegate: NolPayStartPaymentDelegate

    @RelaxedMockK
    lateinit var dataValidatorRegistry: NolPayPaymentDataValidatorRegistry

    @RelaxedMockK
    lateinit var errorMapper: NolPayErrorMapper

    @RelaxedMockK
    lateinit var savedStateHandle: SavedStateHandle

    private lateinit var component: NolPayPaymentComponent

    @BeforeEach
    fun setUp() {
        component = NolPayPaymentComponent(
            startPaymentDelegate,
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
            startPaymentDelegate.logSdkAnalyticsEvent(
                NolPayAnalyticsConstants.PAYMENT_START_METHOD,
                hashMapOf()
            )
        }
    }

    @Test
    fun `start should emit CollectStartPaymentData step when NolPayStartPaymentDelegate start was successful`() {
        coEvery { startPaymentDelegate.start() }.returns(Result.success(Unit))
        coEvery { startPaymentDelegate.startListeningForEvents() }.coAnswers {
            suspendCancellableCoroutine {
            }
        }
        runTest {
            component.start()
            assertEquals(
                NolPayPaymentStep.CollectPaymentData,
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
            startPaymentDelegate.logSdkAnalyticsErrors(
                errorMapper.getPrimerError(
                    exception
                )
            )
        }
    }

    @Test
    fun `updateCollectedData should log correct sdk analytics event`() {
        val collectableData = mockk<NolPayPaymentCollectableData>(relaxed = true)
        runTest {
            component.updateCollectedData(collectableData)
        }

        coVerify {
            startPaymentDelegate.logSdkAnalyticsEvent(
                NolPayAnalyticsConstants.PAYMENT_UPDATE_COLLECTED_DATA_METHOD,
                hashMapOf(
                    NolPayAnalyticsConstants.COLLECTED_DATA_SDK_PARAMS to collectableData.toString()
                )
            )
        }
    }

    @Test
    fun `updateCollectedData should emit validation errors when NolPayPaymentDataValidatorRegistry validate was successful`() {
        val collectableData = mockk<NolPayPaymentCollectableData>(relaxed = true)
        coEvery { dataValidatorRegistry.getValidator(any()).validate(any()) }.returns(listOf())
        runTest {
            component.updateCollectedData(collectableData)
            assertEquals(listOf(), component.componentValidationErrors.first())
        }

        coVerify { dataValidatorRegistry.getValidator(any()).validate(any()) }
    }

    @Test
    fun `submit should log correct sdk analytics event`() {
        runTest {
            component.submit()
        }

        coVerify {
            startPaymentDelegate.logSdkAnalyticsEvent(
                NolPayAnalyticsConstants.PAYMENT_SUBMIT_DATA_METHOD,
                hashMapOf()
            )
        }
    }

    @Test
    fun `submit should emit next step when NolPayPaymentDataValidatorRegistry handleCollectedCardData was successful`() {
        coEvery {
            startPaymentDelegate.handleCollectedCardData(
                any()
            )
        }.returns(Result.success(Unit))
        runTest {
            component.submit()
        }

        coVerify { startPaymentDelegate.handleCollectedCardData(any()) }
        coVerify(exactly = 0) { errorMapper.getPrimerError(any()) }
        coVerify(exactly = 0) {
            startPaymentDelegate.logSdkAnalyticsErrors(
                errorMapper.getPrimerError(
                    any()
                )
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
        }.returns(Result.failure<Exception>(exception))
        runTest {
            component.submit()
            assertNotNull(component.componentError.first())
        }

        coVerify { startPaymentDelegate.handleCollectedCardData(any()) }
        coVerify { errorMapper.getPrimerError(exception) }
        coVerify {
            startPaymentDelegate.logSdkAnalyticsErrors(
                errorMapper.getPrimerError(
                    exception
                )
            )
        }
    }
}
