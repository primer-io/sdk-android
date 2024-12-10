package io.primer.android.webRedirectShared.implementation.composer.presentation.delegate

import io.mockk.every
import io.mockk.mockk
import io.primer.android.components.manager.redirect.composable.WebRedirectStep
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.models.PaymentMethodCancelledError
import io.primer.android.errors.domain.models.PrimerUnknownError
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class WebRedirectDelegateTest {

    private lateinit var successHandler: CheckoutSuccessHandler
    private lateinit var errorHandler: CheckoutErrorHandler
    private lateinit var webRedirectDelegate: WebRedirectDelegate

    private val successFlow = MutableSharedFlow<Payment>()
    private val errorFlow = MutableSharedFlow<PrimerError>()

    @BeforeEach
    fun setUp() {
        successHandler = mockk {
            every { checkoutCompleted } returns successFlow
        }
        errorHandler = mockk {
            every { errors } returns errorFlow
        }

        webRedirectDelegate = WebRedirectDelegate(successHandler, errorHandler)
    }

    @Test
    fun `errors filters out PaymentMethodCancelledError`() = runBlockingTest {
        val collectedErrors = mutableListOf<PrimerError>()

        val error1 = mockk<PrimerError>() {
            every { errorId } returns "errorId1"
            every { description } returns "description1"
            every { diagnosticsId } returns "diagnostics1"
        }
        val error2 = PaymentMethodCancelledError("paymentType")
        val error3 = mockk<PrimerError> {
            every { errorId } returns "errorId3"
            every { description } returns "description3"
            every { diagnosticsId } returns "diagnostics3"
        }

        val job = launch {
            webRedirectDelegate.errors().collect { collectedErrors.add(it) }
        }

        errorFlow.emit(error1)
        errorFlow.emit(error2)
        errorFlow.emit(error3)

        job.cancel()

        assertEquals(listOf(error1, error3), collectedErrors)
    }

    @Test
    fun `steps emits Dismissed for PaymentMethodCancelledError`() = runBlockingTest {
        val collectedSteps = mutableListOf<WebRedirectStep>()

        val job = launch {
            webRedirectDelegate.steps().collect { collectedSteps.add(it) }
        }

        successFlow.emit(mockk<Payment>())
        errorFlow.emit(PaymentMethodCancelledError("paymentType"))

        job.cancel()

        assertEquals(listOf(WebRedirectStep.Dismissed), collectedSteps.toList())
    }

    @Test
    fun `steps emits Success for non PaymentMethodCancelledError`() = runBlockingTest {
        val collectedSteps = mutableListOf<WebRedirectStep>()

        val job = launch {
            webRedirectDelegate.steps().collect { collectedSteps.add(it) }
        }

        successFlow.emit(mockk<Payment>())
        errorFlow.emit(PrimerUnknownError("Something went wrong"))

        job.cancel()

        assertEquals(listOf(WebRedirectStep.Success), collectedSteps.toList())
    }
}
