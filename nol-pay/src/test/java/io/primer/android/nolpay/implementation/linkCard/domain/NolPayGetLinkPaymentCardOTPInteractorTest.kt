package io.primer.android.nolpay.implementation.linkCard.domain

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.nolpay.InstantExecutorExtension
import io.primer.android.nolpay.implementation.linkCard.domain.model.NolPayLinkCardOTPParams
import io.primer.nolpay.api.PrimerNolPay
import io.primer.nolpay.api.exceptions.NolPaySdkException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class NolPayGetLinkPaymentCardOTPInteractorTest {

    @RelaxedMockK
    lateinit var nolPay: PrimerNolPay

    private lateinit var interactor: NolPayGetLinkPaymentCardOTPInteractor

    @BeforeEach
    fun setUp() {
        interactor = NolPayGetLinkPaymentCardOTPInteractor(nolPay)
    }

    @Test
    fun `execute should return success when PrimerNolPay getLinkPaymentCardOTP is successful`() {
        val params = mockk<NolPayLinkCardOTPParams>(relaxed = true)
        every { nolPay.getLinkPaymentCardOTP(any(), any(), any()) }.returns(true)

        runTest {
            val result = interactor(params)
            assert(result.isSuccess)
            assert(result.getOrThrow())
        }
    }

    @Test
    fun `execute should return exception when PrimerNolPay getLinkPaymentCardOTP failed`() {
        val params = mockk<NolPayLinkCardOTPParams>(relaxed = true)
        val expectedException = mockk<NolPaySdkException>(relaxed = true)
        every { nolPay.getLinkPaymentCardOTP(any(), any(), any()) } throws expectedException

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
