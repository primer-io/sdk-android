package io.primer.android.components.implementation.completion

import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.PrimerHeadlessUniversalCheckoutListener
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class DefaultCheckoutSuccessHandlerTest {
    @RelaxedMockK
    internal lateinit var analyticsRepository: AnalyticsRepository

    private lateinit var checkoutSuccessHandler: DefaultCheckoutSuccessHandler

    @BeforeEach
    fun setUp() {
        checkoutSuccessHandler = DefaultCheckoutSuccessHandler(analyticsRepository = analyticsRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `handle should emit payment to checkoutCompleted flow`() =
        runTest {
            val payment = mockk<Payment>(relaxed = true)
            val additionalInfo = mockk<PrimerCheckoutAdditionalInfo>(relaxed = true)

            val job =
                launch {
                    val emittedPayment = checkoutSuccessHandler.checkoutCompleted.first()
                    assertEquals(payment, emittedPayment)
                }

            checkoutSuccessHandler.handle(payment, additionalInfo)
            job.cancel()
        }

    @Test
    fun `handle should call onCheckoutCompleted with correct data`() {
        val payment = mockk<Payment>(relaxed = true)
        val additionalInfo = mockk<PrimerCheckoutAdditionalInfo>(relaxed = true)

        val checkoutListener = mockk<PrimerHeadlessUniversalCheckoutListener>(relaxed = true)
        mockkObject(PrimerHeadlessUniversalCheckout)
        every { PrimerHeadlessUniversalCheckout.instance.checkoutListener } returns checkoutListener

        runTest {
            checkoutSuccessHandler.handle(payment, additionalInfo)
        }

        coVerify {
            checkoutListener.onCheckoutCompleted(
                match { it.payment == payment && it.additionalInfo == additionalInfo },
            )
        }

        unmockkObject(PrimerHeadlessUniversalCheckout)
    }
}
