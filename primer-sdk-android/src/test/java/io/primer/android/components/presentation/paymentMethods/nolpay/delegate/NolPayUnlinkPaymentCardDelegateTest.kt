package io.primer.android.components.presentation.paymentMethods.nolpay.delegate

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.components.data.payments.paymentMethods.nolpay.exception.NolPayIllegalValueKey
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetUnlinkPaymentCardOTPInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPaySdkInitInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayUnlinkPaymentCardInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayUnlinkCardOTPParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayUnlinkCardParams
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCardStep
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCollectableData
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayUnlinkPaymentCardDelegate.Companion.PHYSICAL_CARD_KEY
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayUnlinkPaymentCardDelegate.Companion.UNLINKED_TOKEN_KEY
import io.primer.android.data.base.exceptions.IllegalValueException
import io.primer.nolpay.api.exceptions.NolPaySdkException
import io.primer.nolpay.api.models.PrimerNolPaymentCard
import io.primer.nolpay.api.models.PrimerUnlinkCardMetadata
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class NolPayUnlinkPaymentCardDelegateTest {

    @RelaxedMockK
    lateinit var unlinkPaymentCardInteractor: NolPayUnlinkPaymentCardInteractor

    @RelaxedMockK
    lateinit var unlinkPaymentCardOTPInteractor: NolPayGetUnlinkPaymentCardOTPInteractor

    @RelaxedMockK
    lateinit var analyticsInteractor: AnalyticsInteractor

    @RelaxedMockK
    lateinit var initSdkInitInteractor: NolPaySdkInitInteractor

    private lateinit var delegate: NolPayUnlinkPaymentCardDelegate

    @BeforeEach
    fun setUp() {
        delegate = NolPayUnlinkPaymentCardDelegate(
            unlinkPaymentCardOTPInteractor,
            unlinkPaymentCardInteractor,
            analyticsInteractor,
            initSdkInitInteractor
        )
    }

    @Test
    fun `handleCollectedCardData should return IllegalValueException when collected data is null`() {
        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)

        val exception = assertThrows<IllegalValueException> {
            runTest {
                val result = delegate.handleCollectedCardData(null, savedStateHandle)
                assert(result.isFailure)
                result.getOrThrow()
            }
        }
        assertEquals(NolPayIllegalValueKey.COLLECTED_DATA, exception.key)
    }

    @Test
    fun `handleCollectedCardData with NolPayCardAndPhoneData should return CollectOtpData step when NolPayGetUnlinkPaymentCardOTPInteractor execute was successful`() {
        val collectedData =
            NolPayUnlinkCollectableData.NolPayCardAndPhoneData(
                PrimerNolPaymentCard(CARD_NUMBER),
                MOBILE_NUMBER,
                DIALLING_CODE
            )
        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)
        every { savedStateHandle.get<String>(PHYSICAL_CARD_KEY) }
            .returns(CARD_NUMBER)
        coEvery {
            unlinkPaymentCardOTPInteractor(any())
        } returns Result.success(PrimerUnlinkCardMetadata(UNLINK_TOKEN, CARD_NUMBER))

        runTest {
            val result = delegate.handleCollectedCardData(collectedData, savedStateHandle)
            assert(result.isSuccess)
            assert(result.getOrThrow() is NolPayUnlinkCardStep.CollectOtpData)
        }

        coVerify {
            unlinkPaymentCardOTPInteractor(
                NolPayUnlinkCardOTPParams(
                    MOBILE_NUMBER,
                    DIALLING_CODE,
                    CARD_NUMBER
                )
            )
        }
        verify { savedStateHandle[PHYSICAL_CARD_KEY] = CARD_NUMBER }
    }

    @Test
    fun `handleCollectedCardData with NolPayCardAndPhoneData should return NolPaySdkException when NolPayGetUnlinkPaymentCardOTPInteractor execute fails`() {
        val collectedData =
            NolPayUnlinkCollectableData.NolPayCardAndPhoneData(
                PrimerNolPaymentCard(CARD_NUMBER),
                MOBILE_NUMBER,
                DIALLING_CODE
            )
        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)
        every { savedStateHandle.get<String>(PHYSICAL_CARD_KEY) }
            .returns(CARD_NUMBER)

        val expectedException = mockk<NolPaySdkException>(relaxed = true)
        coEvery {
            unlinkPaymentCardOTPInteractor(any())
        } returns Result.failure(expectedException)

        val exception = assertThrows<NolPaySdkException> {
            runTest {
                val result = delegate.handleCollectedCardData(collectedData, savedStateHandle)
                assert(result.isFailure)
                result.getOrThrow()
            }
        }
        assertEquals(expectedException, exception)

        coVerify {
            unlinkPaymentCardOTPInteractor(
                NolPayUnlinkCardOTPParams(
                    MOBILE_NUMBER,
                    DIALLING_CODE,
                    CARD_NUMBER
                )
            )
        }
    }

    @Test
    fun `handleCollectedCardData with NolPayOtpData should return CardUnlinked step when NolPayUnlinkPaymentCardInteractor execute was successful`() {
        val collectedData = NolPayUnlinkCollectableData.NolPayOtpData(OTP_CODE)
        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)
        every { savedStateHandle.get<String>(UNLINKED_TOKEN_KEY) } returns(UNLINK_TOKEN)
        every { savedStateHandle.get<String>(PHYSICAL_CARD_KEY) } returns(CARD_NUMBER)
        coEvery {
            unlinkPaymentCardInteractor(any())
        } returns(Result.success(true))

        runTest {
            val result = delegate.handleCollectedCardData(collectedData, savedStateHandle)

            assert(result.isSuccess)
            assert(result.getOrThrow() is NolPayUnlinkCardStep.CardUnlinked)
            val cardLinkedStep = result.getOrThrow() as NolPayUnlinkCardStep.CardUnlinked
            assertEquals(PrimerNolPaymentCard(CARD_NUMBER), cardLinkedStep.nolPaymentCard)
        }

        coVerify {
            unlinkPaymentCardInteractor(
                NolPayUnlinkCardParams(
                    CARD_NUMBER,
                    OTP_CODE,
                    UNLINK_TOKEN
                )
            )
        }

        verify {
            savedStateHandle.get<String>(UNLINKED_TOKEN_KEY)
                .equals(UNLINK_TOKEN)
        }
        verify {
            savedStateHandle.get<String>(PHYSICAL_CARD_KEY)
                .equals(CARD_NUMBER)
        }
    }

    @Test
    fun `handleCollectedCardData with NolPayOtpData should return NolPaySdkException when NolPayUnlinkPaymentCardInteractor execute fails`() {
        val collectedData = NolPayUnlinkCollectableData.NolPayOtpData(OTP_CODE)
        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)
        every { savedStateHandle.get<String>(UNLINKED_TOKEN_KEY) } returns UNLINK_TOKEN
        every { savedStateHandle.get<String>(PHYSICAL_CARD_KEY) } returns CARD_NUMBER
        val expectedException = mockk<NolPaySdkException>(relaxed = true)
        coEvery {
            unlinkPaymentCardInteractor(any())
        } returns Result.failure(expectedException)

        val exception = assertThrows<NolPaySdkException> {
            runTest {
                val result = delegate.handleCollectedCardData(collectedData, savedStateHandle)

                assert(result.isFailure)
                result.getOrThrow()
            }
        }

        assertEquals(expectedException, exception)

        coVerify {
            unlinkPaymentCardInteractor(
                NolPayUnlinkCardParams(
                    CARD_NUMBER,
                    OTP_CODE,
                    UNLINK_TOKEN
                )
            )
        }

        verify {
            savedStateHandle.get<String>(UNLINKED_TOKEN_KEY).equals(UNLINK_TOKEN)
        }
        verify {
            savedStateHandle.get<String>(PHYSICAL_CARD_KEY).equals(CARD_NUMBER)
        }
    }

    private companion object {

        const val UNLINK_TOKEN = "ab-bb-cc-dd"
        const val CARD_NUMBER = "123456789"
        const val OTP_CODE = "123456"
        const val MOBILE_NUMBER = "12345678"
        const val DIALLING_CODE = "+1"
    }
}
