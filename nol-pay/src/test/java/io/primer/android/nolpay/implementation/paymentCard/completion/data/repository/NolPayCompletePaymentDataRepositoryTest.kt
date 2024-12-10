package io.primer.android.nolpay.implementation.paymentCard.completion.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.core.data.model.EmptyDataResponse
import io.primer.android.core.data.network.exception.JsonDecodingException
import io.primer.android.nolpay.implementation.paymentCard.completion.data.datasource.RemoteNolPayCompletePaymentDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
internal class NolPayCompletePaymentDataRepositoryTest {

    private lateinit var repository: NolPayCompletePaymentDataRepository
    private val completePaymentDataSource: RemoteNolPayCompletePaymentDataSource = mockk()

    @BeforeEach
    fun setUp() {
        repository = NolPayCompletePaymentDataRepository(completePaymentDataSource)
    }

    @Test
    fun `completePayment should execute successfully`() {
        // Given
        val completeUrl = "https://example.com/complete"
        coEvery { completePaymentDataSource.execute(completeUrl) } returns EmptyDataResponse()

        // When/Then
        assertDoesNotThrow {
            runTest {
                repository.completePayment(completeUrl).getOrThrow()
            }
        }

        coVerify(exactly = 1) { completePaymentDataSource.execute(completeUrl) }
    }

    @Test
    fun `completePayment should handle JsonDecodingException gracefully`() {
        // Given
        val completeUrl = "https://example.com/complete"
        coEvery { completePaymentDataSource.execute(completeUrl) } throws JsonDecodingException(Exception())

        // When/Then
        assertDoesNotThrow {
            runTest {
                repository.completePayment(completeUrl).getOrThrow()
            }
        }

        coVerify(exactly = 1) { completePaymentDataSource.execute(completeUrl) }
    }

    @Test
    fun `completePayment should rethrow other exceptions`() {
        // Given
        val completeUrl = "https://example.com/complete"
        val exception = RuntimeException("Unexpected error")
        coEvery { completePaymentDataSource.execute(completeUrl) } throws exception

        // When/Then
        val thrownException = assertThrows<RuntimeException> {
            runTest {
                repository.completePayment(completeUrl).getOrThrow()
            }
        }
        assert(thrownException.message == "Unexpected error")

        coVerify(exactly = 1) { completePaymentDataSource.execute(completeUrl) }
    }
}
