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
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.payments.create.model.Payment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class DefaultCheckoutErrorHandlerTest {
    @RelaxedMockK
    internal lateinit var analyticsRepository: AnalyticsRepository

    @RelaxedMockK
    internal lateinit var config: PrimerConfig

    private lateinit var checkoutErrorHandler: DefaultCheckoutErrorHandler

    @BeforeEach
    fun setUp() {
        checkoutErrorHandler = DefaultCheckoutErrorHandler(analyticsRepository = analyticsRepository, config = config)
    }

    @Test
    fun `handle should emit error to errors flow`() =
        runTest {
            val error = mockk<PrimerError>(relaxed = true)

            val job =
                launch {
                    val emittedError = checkoutErrorHandler.errors.first()
                    assertEquals(error, emittedError)
                }

            checkoutErrorHandler.handle(error, null)
            job.cancel()
        }

    @Test
    fun `handle should call onFailed with correct data when payment is not null and payment handling is AUTO`() {
        val error = mockk<PrimerError>(relaxed = true)
        val payment = mockk<Payment>(relaxed = true)

        val checkoutListener = mockk<PrimerHeadlessUniversalCheckoutListener>(relaxed = true)
        mockkObject(PrimerHeadlessUniversalCheckout)
        every { PrimerHeadlessUniversalCheckout.instance.checkoutListener } returns checkoutListener

        every { config.settings.paymentHandling } returns PrimerPaymentHandling.AUTO

        runTest {
            checkoutErrorHandler.handle(error, payment)
        }

        coVerify {
            checkoutListener.onFailed(
                error = error,
                checkoutData = match { it.payment == payment },
            )
        }

        unmockkObject(PrimerHeadlessUniversalCheckout)
    }

    @Test
    fun `handle should call onFailed with correct data when payment is null and payment handling is AUTO`() {
        val error = mockk<PrimerError>(relaxed = true)

        val checkoutListener = mockk<PrimerHeadlessUniversalCheckoutListener>(relaxed = true)
        mockkObject(PrimerHeadlessUniversalCheckout)
        every { PrimerHeadlessUniversalCheckout.instance.checkoutListener } returns checkoutListener

        every { config.settings.paymentHandling } returns PrimerPaymentHandling.AUTO

        runTest {
            checkoutErrorHandler.handle(error, null)
        }

        coVerify {
            checkoutListener.onFailed(
                error = error,
                checkoutData = null,
            )
        }

        unmockkObject(PrimerHeadlessUniversalCheckout)
    }

    @Test
    fun `handle should call onFailed without payment data when payment handling is MANUAL`() {
        val error = mockk<PrimerError>(relaxed = true)

        val checkoutListener = mockk<PrimerHeadlessUniversalCheckoutListener>(relaxed = true)
        mockkObject(PrimerHeadlessUniversalCheckout)
        every { PrimerHeadlessUniversalCheckout.instance.checkoutListener } returns checkoutListener

        every { config.settings.paymentHandling } returns PrimerPaymentHandling.MANUAL

        runTest {
            checkoutErrorHandler.handle(error, null)
        }

        coVerify {
            checkoutListener.onFailed(
                error = error,
            )
        }

        unmockkObject(PrimerHeadlessUniversalCheckout)
    }
}
