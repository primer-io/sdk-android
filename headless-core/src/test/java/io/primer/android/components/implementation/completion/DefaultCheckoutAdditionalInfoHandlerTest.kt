package io.primer.android.components.implementation.completion

import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.PrimerHeadlessUniversalCheckoutListener
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.create.domain.model.PaymentResult
import io.primer.android.payments.core.create.domain.repository.PaymentResultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class DefaultCheckoutAdditionalInfoHandlerTest {

    @MockK
    private lateinit var config: PrimerConfig

    @MockK
    private lateinit var paymentResultRepository: PaymentResultRepository

    @MockK
    private lateinit var analyticsRepository: AnalyticsRepository

    @MockK
    private lateinit var checkoutListener: PrimerHeadlessUniversalCheckoutListener

    @MockK
    private lateinit var checkoutAdditionalInfo: PrimerCheckoutAdditionalInfo

    @InjectMockKs
    private lateinit var handler: DefaultCheckoutAdditionalInfoHandler

    @Test
    fun `handle() calls onResumePending when completesCheckout is true and paymentHandling is MANUAL`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        every { config.settings.paymentHandling } returns PrimerPaymentHandling.MANUAL
        every { checkoutAdditionalInfo.completesCheckout } returns true
        mockkObject(PrimerHeadlessUniversalCheckout)
        every {
            PrimerHeadlessUniversalCheckout.instance.checkoutListener
        } returns checkoutListener
        every { checkoutListener.onResumePending(any()) } just Runs

        every { analyticsRepository.addEvent(any()) } just Runs

        handler.handle(checkoutAdditionalInfo)
        advanceUntilIdle()

        verify {
            checkoutListener.onResumePending(checkoutAdditionalInfo)
        }
        unmockkObject(PrimerHeadlessUniversalCheckout)
        Dispatchers.resetMain()
    }

    @Test
    fun `handle() calls onCheckoutCompleted when completesCheckout is true and paymentHandling is AUTO`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        every { config.settings.paymentHandling } returns PrimerPaymentHandling.AUTO
        every { checkoutAdditionalInfo.completesCheckout } returns true
        val payment = mockk<Payment>()
        val paymentResult = mockk<PaymentResult> {
            every { this@mockk.payment } returns payment
        }
        every { paymentResultRepository.getPaymentResult() } returns paymentResult
        mockkObject(PrimerHeadlessUniversalCheckout)
        every {
            PrimerHeadlessUniversalCheckout.instance.checkoutListener
        } returns checkoutListener
        every { checkoutListener.onCheckoutCompleted(any()) } just Runs
        every { analyticsRepository.addEvent(any()) } just Runs

        handler.handle(checkoutAdditionalInfo)
        advanceUntilIdle()

        verify {
            paymentResultRepository.getPaymentResult()
            checkoutListener.onCheckoutCompleted(
                PrimerCheckoutData(payment, checkoutAdditionalInfo)
            )
        }
        unmockkObject(PrimerHeadlessUniversalCheckout)
        Dispatchers.resetMain()
    }

    @Test
    fun `handle() calls onCheckoutAdditionalInfoReceived when completesCheckout is false`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        every { checkoutAdditionalInfo.completesCheckout } returns false
        mockkObject(PrimerHeadlessUniversalCheckout)
        every {
            PrimerHeadlessUniversalCheckout.instance.checkoutListener
        } returns checkoutListener
        every { checkoutListener.onCheckoutAdditionalInfoReceived(checkoutAdditionalInfo) } just Runs

        every { analyticsRepository.addEvent(any()) } just Runs

        handler.handle(checkoutAdditionalInfo)
        advanceUntilIdle()

        verify {
            checkoutListener.onCheckoutAdditionalInfoReceived(checkoutAdditionalInfo)
        }
        unmockkObject(PrimerHeadlessUniversalCheckout)
        Dispatchers.resetMain()
    }
}
