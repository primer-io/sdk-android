package io.primer.android.paypal.implementation.payment.presentation.delegate.presentation

import io.mockk.mockk
import io.mockk.unmockkAll
import io.primer.android.errors.data.exception.UnhandledPaymentPendingStateException
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PaypalPaymentDelegateTest {

    private lateinit var paymentMethodTokenHandler: PaymentMethodTokenHandler
    private lateinit var resumePaymentHandler: PaymentResumeHandler
    private lateinit var successHandler: CheckoutSuccessHandler
    private lateinit var errorHandler: CheckoutErrorHandler
    private lateinit var baseErrorResolver: BaseErrorResolver
    private lateinit var delegate: PaypalPaymentDelegate

    @BeforeEach
    fun setUp() {
        paymentMethodTokenHandler = mockk(relaxed = true)
        resumePaymentHandler = mockk(relaxed = true)
        successHandler = mockk(relaxed = true)
        errorHandler = mockk(relaxed = true)
        baseErrorResolver = mockk(relaxed = true)
        delegate = PaypalPaymentDelegate(
            paymentMethodTokenHandler,
            resumePaymentHandler,
            successHandler,
            errorHandler,
            baseErrorResolver
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `handleNewClientToken should return failure with UnhandledPaymentPendingStateException`() = runTest {
        // Arrange
        val clientToken = "dummy_client_token"
        val payment: Payment? = null

        // Act
        val result = delegate.handleNewClientToken(clientToken, payment)

        // Assert
        Assertions.assertTrue(result.isFailure)
        assertIs<UnhandledPaymentPendingStateException>(result.exceptionOrNull())
        assertEquals("Pending state for PAYPAL is not handled", result.exceptionOrNull()?.message)
    }
}
