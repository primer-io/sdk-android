package io.primer.android.sandboxProcessor.implementation.payment.delegate

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.primer.android.errors.data.exception.UnhandledPaymentPendingStateException
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SandboxProcessorTestPaymentDelegate {

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

    @MockK
    private lateinit var tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository

    private lateinit var delegate: SandboxProcessorPaymentDelegate

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        delegate = SandboxProcessorPaymentDelegate(
            paymentMethodTokenHandler,
            resumePaymentHandler,
            successHandler,
            errorHandler,
            baseErrorResolver,
            tokenizedPaymentMethodRepository
        )
    }

    @Test
    fun `handleNewClientToken should return failure with UnhandledPaymentPendingStateException`() = runTest {
        val clientToken = "someClientToken"
        val payment: Payment? = null
        every { tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType } returns "SomePaymentMethod"

        val result = delegate.handleNewClientToken(clientToken, payment)

        assertTrue(result.isFailure)
        assertIs<UnhandledPaymentPendingStateException>(result.exceptionOrNull())
        assertEquals("Pending state for SomePaymentMethod is not handled", result.exceptionOrNull()?.message)
    }
}
