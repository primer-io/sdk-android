package io.primer.android.nolpay.implementation.paymentCard.completion.domain

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.nolpay.implementation.common.domain.repository.NolPayCompletePaymentRepository
import io.primer.android.nolpay.implementation.paymentCard.completion.domain.model.NolPayCompletePaymentParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
internal class NolPayCompletePaymentInteractorTest {
    private lateinit var interactor: NolPayCompletePaymentInteractor
    private val completePaymentRepository: NolPayCompletePaymentRepository = mockk()

    @BeforeEach
    fun setUp() {
        interactor = NolPayCompletePaymentInteractor(completePaymentRepository)
    }

    @Test
    fun `performAction should call completePayment with correct params`() =
        runTest {
            // Given
            val completeUrl = "https://example.com/complete"
            val params = NolPayCompletePaymentParams(completeUrl)

            coEvery { completePaymentRepository.completePayment(completeUrl) } returns Result.success(Unit)

            // When
            interactor(params)

            // Then
            coVerify(exactly = 1) { completePaymentRepository.completePayment(completeUrl) }
        }

    @Test
    fun `performAction should handle exception from completePayment`() =
        runTest {
            // Given
            val completeUrl = "https://example.com/complete"
            val params = NolPayCompletePaymentParams(completeUrl)
            val exception = Exception("Unexpected error")

            coEvery { completePaymentRepository.completePayment(completeUrl) } returns Result.failure(exception)

            // When/Then
            val result = interactor(params)

            assert(result.isFailure)
            assert(result.exceptionOrNull() == exception)

            coVerify(exactly = 1) { completePaymentRepository.completePayment(completeUrl) }
        }
}
