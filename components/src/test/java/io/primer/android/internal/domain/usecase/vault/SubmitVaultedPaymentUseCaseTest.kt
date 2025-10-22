package io.primer.android.internal.domain.usecase.vault

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import io.primer.android.internal.domain.repositories.PrimerVaultManagerRepository
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData

@OptIn(ExperimentalCoroutinesApi::class)
class SubmitVaultedPaymentUseCaseTest {

    private lateinit var repository: PrimerVaultManagerRepository
    private lateinit var useCase: SubmitVaultedPaymentUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk()
        useCase = SubmitVaultedPaymentUseCase(repository)
    }

    @Test
    fun `invoke should start payment flow without additional data`() = runTest {
        // Given
        coEvery { repository.startPaymentFlow("vault-id", null) } returns Result.success(Unit)

        // When
        val result = useCase("vault-id")

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.startPaymentFlow("vault-id", null) }
    }

    @Test
    fun `invoke should start payment flow with additional data when provided`() = runTest {
        // Given
        val additionalData = mockk<PrimerVaultedPaymentMethodAdditionalData>()
        coEvery { repository.startPaymentFlow("vault-id", additionalData) } returns Result.success(Unit)

        // When
        val result = useCase("vault-id", additionalData)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.startPaymentFlow("vault-id", additionalData) }
    }

    @Test
    fun `invoke should return failure when manager startPaymentFlow fails`() = runTest {
        // Given
        val expected = IllegalStateException("Unable to start")
        coEvery { repository.startPaymentFlow("vault-id", null) } returns Result.failure(expected)

        // When
        val result = useCase("vault-id")

        // Then
        assertTrue(result.isFailure)
        assertSame(expected, result.exceptionOrNull())
        coVerify(exactly = 1) { repository.startPaymentFlow("vault-id", null) }
    }

    @Test
    fun `invoke should return failure when manager retrieval fails`() = runTest {
        // Given
        val expected = RuntimeException("Manager unavailable")
        coEvery { repository.startPaymentFlow("vault-id", null) } returns Result.failure(expected)

        // When
        val result = useCase("vault-id")

        // Then
        assertTrue(result.isFailure)
        assertSame(expected, result.exceptionOrNull())
        coVerify(exactly = 1) { repository.startPaymentFlow("vault-id", null) }
    }
}
