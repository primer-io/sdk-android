package io.primer.android.internal.domain.usecase.vault

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.internal.domain.repositories.PrimerVaultManagerRepository
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ValidateVaultedCVVUseCaseTest {

    private lateinit var repository: PrimerVaultManagerRepository
    private lateinit var useCase: ValidateVaultedCVVUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk()
        useCase = ValidateVaultedCVVUseCase(repository)
    }

    @Test
    fun `invoke should return validation errors when manager validation succeeds`() = runTest {
        // Given
        val additionalData = mockk<PrimerVaultedPaymentMethodAdditionalData>()
        val expectedErrors = listOf(mockk<PrimerValidationError>())
        coEvery { repository.validate("vault-id", additionalData) } returns Result.success(expectedErrors)

        // When
        val result = useCase("vault-id", additionalData)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedErrors, result.getOrNull())
        coVerify(exactly = 1) { repository.validate("vault-id", additionalData) }
    }

    @Test
    fun `invoke should return failure when manager validation fails`() = runTest {
        // Given
        val additionalData = mockk<PrimerVaultedPaymentMethodAdditionalData>()
        val expected = IllegalArgumentException("Invalid CVV")
        coEvery { repository.validate("vault-id", additionalData) } returns Result.failure(expected)

        // When
        val result = useCase("vault-id", additionalData)

        // Then
        assertTrue(result.isFailure)
        assertSame(expected, result.exceptionOrNull())
        coVerify(exactly = 1) { repository.validate("vault-id", additionalData) }
    }

    @Test
    fun `invoke should return failure when manager retrieval fails`() = runTest {
        // Given
        val additionalData = mockk<PrimerVaultedPaymentMethodAdditionalData>()
        val expected = RuntimeException("Manager unavailable")
        coEvery { repository.validate("vault-id", additionalData) } returns Result.failure(expected)

        // When
        val result = useCase("vault-id", additionalData)

        // Then
        assertTrue(result.isFailure)
        assertSame(expected, result.exceptionOrNull())
        coVerify(exactly = 1) { repository.validate("vault-id", additionalData) }
    }
}
