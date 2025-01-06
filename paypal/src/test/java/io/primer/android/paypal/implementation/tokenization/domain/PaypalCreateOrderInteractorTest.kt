package io.primer.android.paypal.implementation.tokenization.domain

import io.mockk.coEvery
import io.mockk.mockk
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalCreateOrderParams
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrder
import io.primer.android.paypal.implementation.tokenization.domain.repository.PaypalCreateOrderRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class PaypalCreateOrderInteractorTest {
    private lateinit var interactor: PaypalCreateOrderInteractor
    private val createOrderRepository: PaypalCreateOrderRepository = mockk()

    @BeforeEach
    fun setUp() {
        interactor =
            PaypalCreateOrderInteractor(
                createOrderRepository,
            )
    }

    @Test
    fun `performAction() should call createOrder with the provided params`() =
        runTest {
            // Given
            val params = mockk<PaypalCreateOrderParams>(relaxed = true)

            val expectedResult = mockk<PaypalOrder>(relaxed = true)

            coEvery {
                createOrderRepository.createOrder(params)
            } returns Result.success(expectedResult)

            // When
            val result = interactor(params)

            // Then
            assertEquals(Result.success(expectedResult), result)
        }
}
