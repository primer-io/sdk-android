package io.primer.android.components.domain.payments.paymentMethods.stripe.ach

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.primer.android.components.domain.payments.paymentMethods.stripe.ach.model.StripeAchCompletePaymentParams
import io.primer.android.components.domain.payments.paymentMethods.stripe.ach.repository.StripeAchCompletePaymentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class StripeAchCompletePaymentInteractorTest {
    @MockK
    private lateinit var completePaymentRepository: StripeAchCompletePaymentRepository

    @InjectMockKs
    private lateinit var interactor: StripeAchCompletePaymentInteractor

    @Test
    fun `invoke() should delegate to repository when payment method id is not null`() = runTest {
        val expected = Result.success(Unit)
        coEvery { completePaymentRepository.completePayment(any(), any(), any()) } returns expected

        val result = interactor.invoke(
            StripeAchCompletePaymentParams(
                completeUrl = "completeUrl",
                mandateTimestamp = "mandateTimestamp",
                paymentMethodId = "paymentMethodId"
            )
        )

        assertEquals(expected, result)
        coVerify {
            completePaymentRepository.completePayment(
                completeUrl = "completeUrl",
                mandateTimestamp = "mandateTimestamp",
                paymentMethodId = "paymentMethodId"
            )
        }
        confirmVerified(completePaymentRepository)
    }

    @Test
    fun `invoke() should delegate to repository when payment method id is null`() = runTest {
        val expected = Result.success(Unit)
        coEvery { completePaymentRepository.completePayment(any(), any(), any()) } returns expected

        val result = interactor.invoke(
            StripeAchCompletePaymentParams(
                completeUrl = "completeUrl",
                mandateTimestamp = "mandateTimestamp",
                paymentMethodId = null
            )
        )

        assertEquals(expected, result)
        coVerify {
            completePaymentRepository.completePayment(
                completeUrl = "completeUrl",
                mandateTimestamp = "mandateTimestamp",
                paymentMethodId = null
            )
        }
        confirmVerified(completePaymentRepository)
    }
}
