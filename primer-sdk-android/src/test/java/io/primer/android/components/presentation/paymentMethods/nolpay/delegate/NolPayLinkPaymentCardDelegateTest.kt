package io.primer.android.components.presentation.paymentMethods.nolpay.delegate

import android.nfc.Tag
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.components.data.payments.paymentMethods.nolpay.exception.NolPayIllegalValueKey
import io.primer.android.components.domain.payments.metadata.phone.PhoneMetadataInteractor
import io.primer.android.components.domain.payments.metadata.phone.model.PhoneMetadata
import io.primer.android.components.domain.payments.metadata.phone.model.PhoneMetadataParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardOTPInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardTokenInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayLinkPaymentCardInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPaySdkInitInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayLinkCardOTPParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayLinkCardParams
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayTagParams
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCardStep
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCollectableData
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayLinkPaymentCardDelegate.Companion.LINKED_TOKEN_KEY
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayLinkPaymentCardDelegate.Companion.PHYSICAL_CARD_KEY
import io.primer.android.data.base.exceptions.IllegalValueException
import io.primer.nolpay.api.exceptions.NolPaySdkException
import io.primer.nolpay.api.models.PrimerLinkCardMetadata
import io.primer.nolpay.api.models.PrimerNolPaymentCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class NolPayLinkPaymentCardDelegateTest {

    @RelaxedMockK
    lateinit var getLinkPaymentCardTokenInteractor: NolPayGetLinkPaymentCardTokenInteractor

    @RelaxedMockK
    lateinit var linkPaymentCardInteractor: NolPayLinkPaymentCardInteractor

    @RelaxedMockK
    lateinit var phoneMetadataInteractor: PhoneMetadataInteractor

    @RelaxedMockK
    lateinit var linkPaymentCardOTPInteractor: NolPayGetLinkPaymentCardOTPInteractor

    @RelaxedMockK
    lateinit var initSdkInitInteractor: NolPaySdkInitInteractor

    private lateinit var delegate: NolPayLinkPaymentCardDelegate

    @BeforeEach
    fun setUp() {
        delegate = NolPayLinkPaymentCardDelegate(
            getLinkPaymentCardTokenInteractor,
            linkPaymentCardOTPInteractor,
            linkPaymentCardInteractor,
            phoneMetadataInteractor,
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
    fun `handleCollectedCardData with NolPayTagData should return CollectPhoneData step when NolPayGetLinkPaymentCardTokenInteractor execute was successful`() {
        val tag = mockk<Tag>(relaxed = true)
        val collectedData = NolPayLinkCollectableData.NolPayTagData(tag)
        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)
        coEvery {
            getLinkPaymentCardTokenInteractor(any())
        } returns (Result.success(PrimerLinkCardMetadata(LINK_TOKEN, CARD_NUMBER)))

        runTest {
            val result = delegate.handleCollectedCardData(collectedData, savedStateHandle)
            assert(result.isSuccess)
            assert(result.getOrThrow() is NolPayLinkCardStep.CollectPhoneData)
            val collectPhoneDataStep = result.getOrThrow() as NolPayLinkCardStep.CollectPhoneData
            assertEquals(CARD_NUMBER, collectPhoneDataStep.cardNumber)
        }

        coVerify { getLinkPaymentCardTokenInteractor(NolPayTagParams(tag)) }
        verify { savedStateHandle[PHYSICAL_CARD_KEY] = CARD_NUMBER }
        verify { savedStateHandle[LINKED_TOKEN_KEY] = LINK_TOKEN }
    }

    @Test
    fun `handleCollectedCardData with NolPayTagData should emit NolPaySdkException step when NolPayGetLinkPaymentCardTokenInteractor execute fails`() {
        val tag = mockk<Tag>(relaxed = true)
        val collectedData = NolPayLinkCollectableData.NolPayTagData(tag)
        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)
        val expectedException = mockk<NolPaySdkException>(relaxed = true)
        coEvery {
            getLinkPaymentCardTokenInteractor(any())
        } returns (Result.failure(expectedException))

        val exception = assertThrows<NolPaySdkException> {
            runTest {
                val result = delegate.handleCollectedCardData(collectedData, savedStateHandle)
                assert(result.isFailure)
                result.getOrThrow()
            }
        }

        assertEquals(expectedException, exception)

        coVerify { getLinkPaymentCardTokenInteractor(NolPayTagParams(tag)) }
        verify(exactly = 0) { savedStateHandle[PHYSICAL_CARD_KEY] = CARD_NUMBER }
        verify(exactly = 0) { savedStateHandle[LINKED_TOKEN_KEY] = LINK_TOKEN }
    }

    @Test
    fun `handleCollectedCardData with NolPayPhoneData should return CollectOtpData step when PhoneMetadataInteractor execute was successful and NolPayGetLinkPaymentCardOTPInteractor execute was successful`() {
        val collectedData = NolPayLinkCollectableData.NolPayPhoneData(MOBILE_NUMBER)
        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)
        every { savedStateHandle.get<String>(LINKED_TOKEN_KEY) }.returns(LINK_TOKEN)
        val phoneMetadata = mockk<PhoneMetadata>(relaxed = true)
        every { phoneMetadata.countryCode } returns DIALLING_CODE
        every { phoneMetadata.nationalNumber } returns MOBILE_NUMBER
        coEvery {
            linkPaymentCardOTPInteractor(any())
        } returns (Result.success(true))

        coEvery { phoneMetadataInteractor(any()) }.returns(Result.success(phoneMetadata))

        runTest {
            val result = delegate.handleCollectedCardData(collectedData, savedStateHandle)
            assert(result.isSuccess)
            val linkCardStep = result.getOrThrow()
            assert(linkCardStep is NolPayLinkCardStep.CollectOtpData)
            assertEquals(
                MOBILE_NUMBER,
                (linkCardStep as NolPayLinkCardStep.CollectOtpData).mobileNumber
            )
        }

        coVerify {
            linkPaymentCardOTPInteractor(
                NolPayLinkCardOTPParams(
                    MOBILE_NUMBER,
                    DIALLING_CODE,
                    LINK_TOKEN
                )
            )
        }

        coVerify {
            phoneMetadataInteractor(PhoneMetadataParams(collectedData.mobileNumber))
        }
    }

    @Test
    fun `handleCollectedCardData with NolPayPhoneData should return NolPaySdkException step when PhoneMetadataInteractor execute was successful and NolPayGetLinkPaymentCardOTPInteractor execute fails`() {
        val collectedData = NolPayLinkCollectableData.NolPayPhoneData(MOBILE_NUMBER)
        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)
        every { savedStateHandle.get<String>(LINKED_TOKEN_KEY) }.returns(LINK_TOKEN)
        val phoneMetadata = mockk<PhoneMetadata>(relaxed = true)
        every { phoneMetadata.countryCode } returns DIALLING_CODE
        every { phoneMetadata.nationalNumber } returns MOBILE_NUMBER
        val expectedException = mockk<NolPaySdkException>(relaxed = true)
        coEvery {
            linkPaymentCardOTPInteractor(any())
        } returns (Result.failure(expectedException))
        coEvery { phoneMetadataInteractor(any()) }.returns(Result.success(phoneMetadata))

        val exception = assertThrows<NolPaySdkException> {
            runTest {
                val result = delegate.handleCollectedCardData(collectedData, savedStateHandle)
                assert(result.isFailure)
                result.getOrThrow()
            }
        }
        assertEquals(expectedException, exception)

        coVerify {
            linkPaymentCardOTPInteractor(
                NolPayLinkCardOTPParams(
                    MOBILE_NUMBER,
                    DIALLING_CODE,
                    LINK_TOKEN
                )
            )
        }

        coVerify {
            phoneMetadataInteractor(PhoneMetadataParams(collectedData.mobileNumber))
        }
    }

    @Test
    fun `handleCollectedCardData with NolPayPhoneData should return IllegalValueException step when PhoneMetadataInteractor execute was successful LINKED_TOKEN is missing`() {
        val collectedData = NolPayLinkCollectableData.NolPayPhoneData(MOBILE_NUMBER)
        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)
        every { savedStateHandle.get<String>(LINKED_TOKEN_KEY) }.returns(null)

        val phoneMetadata = mockk<PhoneMetadata>(relaxed = true)
        every { phoneMetadata.countryCode } returns DIALLING_CODE
        every { phoneMetadata.nationalNumber } returns MOBILE_NUMBER

        coEvery {
            linkPaymentCardOTPInteractor(any())
        } returns (Result.success(true))

        coEvery { phoneMetadataInteractor(any()) }.returns(Result.success(phoneMetadata))

        val exception = assertThrows<IllegalValueException> {
            runTest {
                val result = delegate.handleCollectedCardData(collectedData, savedStateHandle)
                assert(result.isFailure)
                result.getOrThrow()
            }
        }
        assertEquals(NolPayIllegalValueKey.SAVED_DATA_LINK_TOKEN, exception.key)

        coVerify(exactly = 0) {
            linkPaymentCardOTPInteractor(any())
        }
        coVerify {
            phoneMetadataInteractor(PhoneMetadataParams(collectedData.mobileNumber))
        }
    }

    @Test
    fun `handleCollectedCardData with NolPayOtpData should return CardLinked step when NolPayLinkPaymentCardInteractor execute was successfull`() {
        val collectedData = NolPayLinkCollectableData.NolPayOtpData(OTP_CODE)
        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)
        every { savedStateHandle.get<String>(LINKED_TOKEN_KEY) } returns LINK_TOKEN
        every { savedStateHandle.get<String>(PHYSICAL_CARD_KEY) } returns CARD_NUMBER
        coEvery {
            linkPaymentCardInteractor(any())
        } returns (Result.success(true))

        runTest {
            val result = delegate.handleCollectedCardData(collectedData, savedStateHandle)

            assert(result.isSuccess)
            assert(result.getOrThrow() is NolPayLinkCardStep.CardLinked)
            val cardLinkedStep = result.getOrThrow() as NolPayLinkCardStep.CardLinked
            assertEquals(PrimerNolPaymentCard(CARD_NUMBER), cardLinkedStep.nolPaymentCard)
        }

        coVerify {
            linkPaymentCardInteractor(
                NolPayLinkCardParams(
                    LINK_TOKEN,
                    OTP_CODE
                )
            )
        }

        verify { savedStateHandle.get<String>(LINKED_TOKEN_KEY).equals(LINK_TOKEN) }
        verify { savedStateHandle.get<String>(PHYSICAL_CARD_KEY).equals(CARD_NUMBER) }
    }

    @Test
    fun `handleCollectedCardData with NolPayOtpData should return NolPaySdkException when NolPayLinkPaymentCardInteractor execute fails`() {
        val collectedData = NolPayLinkCollectableData.NolPayOtpData(OTP_CODE)
        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)
        every { savedStateHandle.get<String>(LINKED_TOKEN_KEY) } returns LINK_TOKEN
        every { savedStateHandle.get<String>(PHYSICAL_CARD_KEY) } returns CARD_NUMBER
        val expectedException = mockk<NolPaySdkException>(relaxed = true)
        coEvery {
            linkPaymentCardInteractor(any())
        }.returns(Result.failure(expectedException))

        val exception = assertThrows<NolPaySdkException> {
            runTest {
                val result = delegate.handleCollectedCardData(collectedData, savedStateHandle)

                assert(result.isFailure)
                result.getOrThrow()
            }
        }

        assertEquals(expectedException, exception)

        coVerify {
            linkPaymentCardInteractor(
                NolPayLinkCardParams(
                    LINK_TOKEN,
                    OTP_CODE
                )
            )
        }

        verify { savedStateHandle.get<String>(LINKED_TOKEN_KEY).equals(LINK_TOKEN) }
        verify(exactly = 0) { savedStateHandle.get<String>(PHYSICAL_CARD_KEY).equals(CARD_NUMBER) }
    }

    private companion object {

        const val LINK_TOKEN = "ab-bb-cc-dd"
        const val CARD_NUMBER = "123456789"
        const val OTP_CODE = "123456"
        const val MOBILE_NUMBER = "12345678"
        const val DIALLING_CODE = "+1"
    }
}
