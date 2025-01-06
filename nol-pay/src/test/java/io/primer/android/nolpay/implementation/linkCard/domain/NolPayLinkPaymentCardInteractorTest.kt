package io.primer.android.nolpay.implementation.linkCard.domain

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.nolpay.implementation.linkCard.domain.model.NolPayLinkCardParams
import io.primer.nolpay.api.PrimerNolPay
import io.primer.nolpay.api.exceptions.NolPaySdkException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class NolPayLinkPaymentCardInteractorTest {
    @RelaxedMockK
    lateinit var nolPay: PrimerNolPay

    private lateinit var interactor: NolPayLinkPaymentCardInteractor

    @BeforeEach
    fun setUp() {
        interactor = NolPayLinkPaymentCardInteractor(nolPay)
    }

    @Test
    fun `execute should return success when PrimerNolPay linkPaymentCard is successful`() {
        val params = mockk<NolPayLinkCardParams>(relaxed = true)
        every { nolPay.linkPaymentCard(any(), any()) }.returns(true)

        runTest {
            val result = interactor(params)
            assert(result.isSuccess)
            assert(result.getOrThrow())
        }
    }

    @Test
    fun `execute should return exception when PrimerNolPay linkPaymentCard failed`() {
        val params = mockk<NolPayLinkCardParams>(relaxed = true)
        val expectedException = mockk<NolPaySdkException>(relaxed = true)
        every { nolPay.linkPaymentCard(any(), any()) } throws expectedException

        val exception =
            assertThrows<NolPaySdkException> {
                runTest {
                    val result = interactor(params)
                    assert(result.isFailure)
                    assert(result.getOrThrow())
                }
            }

        assertEquals(expectedException, exception)
    }
}
