package io.primer.android.components.domain.payments.paymentMethods.nolPay

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetUnlinkPaymentCardOTPInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayUnlinkCardOTPParams
import io.primer.nolpay.api.PrimerNolPay
import io.primer.nolpay.api.exceptions.NolPaySdkException
import io.primer.nolpay.api.models.PrimerUnlinkCardMetadata
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class NolPayGetUnlinkPaymentCardOTPInteractorTest {

    @RelaxedMockK
    lateinit var nolPay: PrimerNolPay

    private lateinit var interactor: NolPayGetUnlinkPaymentCardOTPInteractor

    @BeforeEach
    fun setUp() {
        interactor = NolPayGetUnlinkPaymentCardOTPInteractor(nolPay)
    }

    @Test
    fun `execute should return success when PrimerNolPay getUnlinkPaymentCardOTP is successful`() {
        val params = mockk<NolPayUnlinkCardOTPParams>(relaxed = true)
        val unlinkCardMetadata = mockk<PrimerUnlinkCardMetadata>(relaxed = true)
        every { nolPay.getUnlinkPaymentCardOTP(any(), any(), any()) }.returns(unlinkCardMetadata)

        runTest {
            val result = interactor(params)
            assert(result.isSuccess)
            assertEquals(unlinkCardMetadata, result.getOrThrow())
        }
    }

    @Test
    fun `execute should return exception when PrimerNolPay getUnlinkPaymentCardOTP failed`() {
        val params = mockk<NolPayUnlinkCardOTPParams>(relaxed = true)
        val expectedException = mockk<NolPaySdkException>(relaxed = true)
        every { nolPay.getUnlinkPaymentCardOTP(any(), any(), any()) } throws expectedException

        val exception = assertThrows<NolPaySdkException> {
            runTest {
                val result = interactor(params)
                assert(result.isFailure)
                result.getOrThrow()
            }
        }

        assertEquals(expectedException, exception)
    }
}
