package io.primer.android.components.domain.payments.paymentMethods.nolPay

import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayRequestPaymentInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayRequestPaymentParams
import io.primer.nolpay.api.PrimerNolPay
import io.primer.nolpay.api.exceptions.NolPaySdkException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class NolPayRequestPaymentInteractorTest {

    @RelaxedMockK
    lateinit var nolPay: PrimerNolPay

    private lateinit var interactor: NolPayRequestPaymentInteractor

    @BeforeEach
    fun setUp() {
        interactor = NolPayRequestPaymentInteractor(nolPay)
    }

    @Test
    fun `execute should return success when PrimerNolPay createPayment is successful`() {
        val params = mockk<NolPayRequestPaymentParams>(relaxed = true)
        coEvery { nolPay.createPayment(any(), any()) }.returns(true)

        runTest {
            val result = interactor(params)
            assert(result.isSuccess)
            assertEquals(true, result.getOrThrow())
        }
    }

    @Test
    fun `execute should return exception when PrimerNolPay createPayment failed`() {
        val params = mockk<NolPayRequestPaymentParams>(relaxed = true)
        val expectedException = mockk<NolPaySdkException>(relaxed = true)
        coEvery { nolPay.createPayment(any(), any()) } throws expectedException

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
