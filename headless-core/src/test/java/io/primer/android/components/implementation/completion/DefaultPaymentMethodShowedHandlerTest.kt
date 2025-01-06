package io.primer.android.components.implementation.completion

import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.components.InstantExecutorExtension
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.PrimerHeadlessUniversalCheckoutUiListener
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
class DefaultPaymentMethodShowedHandlerTest {
    @RelaxedMockK
    internal lateinit var analyticsRepository: AnalyticsRepository

    private lateinit var paymentMethodShowedHandler: DefaultPaymentMethodShowedHandler

    @BeforeEach
    fun setUp() {
        paymentMethodShowedHandler = DefaultPaymentMethodShowedHandler(analyticsRepository = analyticsRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `handle should emit payment to paymentMethodShowed flow`() =
        runTest {
            val paymentMethodType = "paymentMethodType"

            val job =
                launch {
                    val emittedPaymentMethodType = paymentMethodShowedHandler.paymentMethodShowed.first()
                    assertEquals(paymentMethodType, emittedPaymentMethodType)
                }

            paymentMethodShowedHandler.handle(paymentMethodType)
            job.cancel()
        }

    @Test
    fun `handle should call onPaymentMethodShowed with correct data`() {
        val paymentMethodType = "paymentMethodType"

        val checkoutUiListener = mockk<PrimerHeadlessUniversalCheckoutUiListener>(relaxed = true)
        mockkObject(PrimerHeadlessUniversalCheckout)
        every { PrimerHeadlessUniversalCheckout.instance.uiListener } returns checkoutUiListener

        runTest {
            paymentMethodShowedHandler.handle(paymentMethodType)
        }

        coVerify {
            checkoutUiListener.onPaymentMethodShowed(paymentMethodType)
        }

        unmockkObject(PrimerHeadlessUniversalCheckout)
    }
}
