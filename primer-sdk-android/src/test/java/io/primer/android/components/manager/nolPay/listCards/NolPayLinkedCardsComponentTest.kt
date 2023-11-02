package io.primer.android.components.manager.nolPay.listCards

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.manager.nolPay.analytics.NolPayAnalyticsConstants
import io.primer.android.components.manager.nolPay.listCards.component.NolPayLinkedCardsComponent
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayGetLinkedCardsDelegate
import io.primer.nolpay.api.exceptions.NolPaySdkException
import io.primer.nolpay.api.models.PrimerNolPaymentCard
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class NolPayLinkedCardsComponentTest {

    @RelaxedMockK
    lateinit var linkedCardsDelegate: NolPayGetLinkedCardsDelegate

    private lateinit var component: NolPayLinkedCardsComponent

    @BeforeEach
    fun setUp() {
        component = NolPayLinkedCardsComponent(linkedCardsDelegate)
    }

    @Test
    fun `getLinkedCards should return linked cards when NolPayGetLinkedCardsDelegate getLinkedCards was successful`() {
        val nolPaymentCard = mockk<PrimerNolPaymentCard>(relaxed = true)
        coEvery {
            linkedCardsDelegate.getLinkedCards(any())
        } returns Result.success(listOf(nolPaymentCard))

        runTest {
            val result = component.getLinkedCards(MOBILE_NUMBER)
            assert(result.isSuccess)
            val cards = result.getOrThrow()
            assertEquals(listOf(nolPaymentCard), cards)
        }

        coVerify {
            linkedCardsDelegate.getLinkedCards(MOBILE_NUMBER)
        }
    }

    @Test
    fun `getLinkedCards should return error when NolPayGetLinkedCardsDelegate getLinkedCards failed`() {
        val expectedException = mockk<NolPaySdkException>(relaxed = true)
        coEvery {
            linkedCardsDelegate.getLinkedCards(any())
        } returns Result.failure(expectedException)

        val exception = assertThrows<NolPaySdkException> {
            runTest {
                val result = component.getLinkedCards(MOBILE_NUMBER)
                assert(result.isFailure)
                result.getOrThrow()
            }
        }

        assertEquals(expectedException, exception)

        coVerify {
            linkedCardsDelegate.getLinkedCards(MOBILE_NUMBER)
        }
    }

    @Test
    fun `getLinkedCards should log correct sdk analytics event`() {
        runTest {
            component.getLinkedCards(MOBILE_NUMBER)
        }

        coVerify {
            linkedCardsDelegate.logSdkAnalyticsEvent(
                NolPayAnalyticsConstants.LINKED_CARDS_GET_CARDS_METHOD,
                hashMapOf("category" to PrimerPaymentMethodManagerCategory.NOL_PAY.name)
            )
        }
    }

    private companion object {
        const val MOBILE_NUMBER = "+9713443434"
    }
}
