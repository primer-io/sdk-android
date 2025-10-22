package io.primer.android.internal.domain.usecase.vault

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.internal.domain.repositories.PrimerVaultManagerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FetchVaultedPaymentMethodsUseCaseTest {

    private lateinit var repository: PrimerVaultManagerRepository
    private lateinit var useCase: FetchVaultedPaymentMethodsUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk()
        useCase = FetchVaultedPaymentMethodsUseCase(repository)
    }

    @Test
    fun `invoke should return vaulted payment methods when manager succeeds`() = runTest {
        // Given
        val expectedMethods = listOf(mockk<PrimerVaultedPaymentMethod>())
        coEvery { repository.fetchVaultedPaymentMethods() } returns Result.success(expectedMethods)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedMethods, result.getOrNull())
        coVerify(exactly = 1) { repository.fetchVaultedPaymentMethods() }
    }

    @Test
    fun `invoke should return failure when manager fetch fails`() = runTest {
        // Given
        val expected = IllegalStateException("Fetch failure")
        coEvery { repository.fetchVaultedPaymentMethods() } returns Result.failure(expected)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        assertSame(expected, result.exceptionOrNull())
        coVerify(exactly = 1) { repository.fetchVaultedPaymentMethods() }
    }

    @Test
    fun `invoke should return failure when manager retrieval fails`() = runTest {
        // Given
        val expected = RuntimeException("Manager unavailable")
        coEvery { repository.fetchVaultedPaymentMethods() } returns Result.failure(expected)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        assertSame(expected, result.exceptionOrNull())
        coVerify(exactly = 1) { repository.fetchVaultedPaymentMethods() }
    }
}
