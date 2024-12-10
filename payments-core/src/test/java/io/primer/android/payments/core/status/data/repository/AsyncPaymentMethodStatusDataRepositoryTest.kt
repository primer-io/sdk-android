package io.primer.android.payments.core.status.data.repository

import io.mockk.coEvery
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.payments.InstantExecutorExtension
import io.primer.android.payments.core.errors.data.exception.AsyncFlowIncompleteException
import io.primer.android.payments.core.status.data.datasource.RemoteAsyncPaymentMethodStatusDataSource
import io.primer.android.payments.core.status.data.models.AsyncMethodStatus
import io.primer.android.payments.core.status.data.models.AsyncPaymentMethodStatusDataResponse
import io.primer.android.payments.core.status.domain.model.AsyncStatus
import io.primer.android.payments.toListDuring
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class AsyncPaymentMethodStatusDataRepositoryTest {

    private lateinit var dataSource: RemoteAsyncPaymentMethodStatusDataSource
    private lateinit var repository: AsyncPaymentMethodStatusDataRepository

    @BeforeEach
    fun setup() {
        dataSource = mockk()
        repository = AsyncPaymentMethodStatusDataRepository(dataSource)
    }

    @Test
    fun `getAsyncStatus should return AsyncStatus when status is COMPLETE`() = runTest {
        // Given
        val url = "http://test.url"
        val response = AsyncPaymentMethodStatusDataResponse(
            id = "test_id",
            status = AsyncMethodStatus.COMPLETE,
            source = "test_source"
        )
        coEvery { dataSource.execute(url) } returns flowOf(response)

        // When
        val result = repository.getAsyncStatus(url).first()

        // Then
        assertEquals(AsyncStatus(resumeToken = "test_id"), result)
    }

    @Disabled
    @Test
    fun `getAsyncStatus should retry when status is not COMPLETE`() = runTest {
        // Given
        val url = "http://test.url"
        val incompleteResponse = AsyncPaymentMethodStatusDataResponse(
            id = "test_id",
            status = AsyncMethodStatus.PENDING,
            source = "test_source"
        )
        val completeResponse = AsyncPaymentMethodStatusDataResponse(
            id = "test_id",
            status = AsyncMethodStatus.COMPLETE,
            source = "test_source"
        )
        coEvery { dataSource.execute(url) } returns flowOf(incompleteResponse, completeResponse)

        // When
        val results = repository.getAsyncStatus(url).toListDuring(2.0.seconds)

        println(results)
        // Then
        assertTrue(results.any { it.resumeToken == "test_id" })
    }

    @Disabled
    @Test
    fun `getAsyncStatus should throw exception for invalid status`() = runTest {
        // Given
        val url = "http://test.url"
        val response = AsyncPaymentMethodStatusDataResponse(
            id = "test_id",
            status = AsyncMethodStatus.PROCESSING,
            source = "test_source"
        )
        coEvery { dataSource.execute(url) } returns flowOf(response)

        // When / Then
        assertThrows<AsyncFlowIncompleteException> {
            repository.getAsyncStatus(url).first()
        }
    }
}
