package io.primer.android.components.domain.payments.paymentMethods.nolPay

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkedCardsInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayGetLinkedCardsParams
import io.primer.nolpay.api.PrimerNolPay
import io.primer.nolpay.api.exceptions.NolPaySdkException
import io.primer.nolpay.api.models.PrimerNolPaymentCard
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class NolPayGetLinkedCardsInteractorTest {

    @RelaxedMockK
    lateinit var nolPay: PrimerNolPay

    private lateinit var interactor: NolPayGetLinkedCardsInteractor

    @BeforeEach
    fun setUp() {
        interactor = NolPayGetLinkedCardsInteractor(nolPay)
    }

    @Test
    fun `execute should return success when PrimerNolPay getLinkedPaymentCards is successful`() {
        val params = mockk<NolPayGetLinkedCardsParams>(relaxed = true)
        val card = mockk<PrimerNolPaymentCard>(relaxed = true)
        every { nolPay.getLinkedPaymentCards(any(), any()) }.returns(listOf(card))

        runTest {
            val result = interactor(params)
            assert(result.isSuccess)
            assertEquals(listOf(card), result.getOrThrow())
        }
    }

    @Test
    fun `execute should return exception when PrimerNolPay getLinkedPaymentCards failed`() {
        val params = mockk<NolPayGetLinkedCardsParams>(relaxed = true)
        val expectedException = mockk<NolPaySdkException>(relaxed = true)
        every { nolPay.getLinkedPaymentCards(any(), any()) } throws expectedException

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
