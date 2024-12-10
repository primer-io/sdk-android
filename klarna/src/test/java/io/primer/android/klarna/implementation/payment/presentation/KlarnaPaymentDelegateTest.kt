package io.primer.android.klarna.implementation.payment.presentation

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.primer.android.errors.data.exception.UnhandledPaymentPendingStateException
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@ExperimentalCoroutinesApi
internal class KlarnaPaymentDelegateTest {

    @MockK
    private lateinit var paymentMethodTokenHandler: PaymentMethodTokenHandler

    @MockK
    private lateinit var resumePaymentHandler: PaymentResumeHandler

    @MockK
    private lateinit var successHandler: CheckoutSuccessHandler

    @MockK
    private lateinit var errorHandler: CheckoutErrorHandler

    @MockK
    private lateinit var baseErrorResolver: BaseErrorResolver

    private lateinit var klarnaPaymentDelegate: KlarnaPaymentDelegate

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        klarnaPaymentDelegate = KlarnaPaymentDelegate(
            paymentMethodTokenHandler,
            resumePaymentHandler,
            successHandler,
            errorHandler,
            baseErrorResolver
        )
    }

    @Test
    fun `handleNewClientToken should return failure with UnhandledPaymentPendingStateException`() = runTest {
        val clientToken = "someClientToken"
        val payment: Payment? = null

        val result = klarnaPaymentDelegate.handleNewClientToken(clientToken, payment)

        assertTrue(result.isFailure)
        assertIs<UnhandledPaymentPendingStateException>(result.exceptionOrNull())
        assertEquals("Pending state for KLARNA is not handled", result.exceptionOrNull()?.message)
    }
}
