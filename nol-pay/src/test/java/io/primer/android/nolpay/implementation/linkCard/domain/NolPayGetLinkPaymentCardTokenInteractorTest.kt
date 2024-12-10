package io.primer.android.nolpay.implementation.linkCard.domain

import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.nolpay.implementation.common.domain.model.NolPayTagParams
import io.primer.nolpay.api.PrimerNolPay
import io.primer.nolpay.api.exceptions.NolPaySdkException
import io.primer.nolpay.api.models.PrimerLinkCardMetadata
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class NolPayGetLinkPaymentCardTokenInteractorTest {

    @RelaxedMockK
    lateinit var nolPay: PrimerNolPay

    private lateinit var interactor: NolPayGetLinkPaymentCardTokenInteractor

    @BeforeEach
    fun setUp() {
        interactor = NolPayGetLinkPaymentCardTokenInteractor(nolPay)
    }

    @Test
    fun `execute should return success when PrimerNolPay getLinkPaymentCardToken is successful`() {
        val params = mockk<NolPayTagParams>(relaxed = true)
        val linkCardMetadata = mockk<PrimerLinkCardMetadata>(relaxed = true)
        coEvery { nolPay.getLinkPaymentCardToken(any()) }.returns(linkCardMetadata)

        runTest {
            val result = interactor(params)
            assert(result.isSuccess)
            assertEquals(linkCardMetadata, result.getOrThrow())
        }
    }

    @Test
    fun `execute should return exception when PrimerNolPay getLinkPaymentCardToken failed`() {
        val params = mockk<NolPayTagParams>(relaxed = true)
        val expectedException = mockk<NolPaySdkException>(relaxed = true)
        coEvery { nolPay.getLinkPaymentCardToken(any()) } throws expectedException

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
