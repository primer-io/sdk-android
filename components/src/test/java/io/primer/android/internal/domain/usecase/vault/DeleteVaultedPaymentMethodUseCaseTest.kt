package io.primer.android.internal.domain.usecase.vault

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.internal.domain.repositories.PrimerVaultManagerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteVaultedPaymentMethodUseCaseTest {

    private lateinit var repository: PrimerVaultManagerRepository
    private lateinit var useCase: DeleteVaultedPaymentMethodUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk()
        useCase = DeleteVaultedPaymentMethodUseCase(repository)
    }

    @Test
    fun `invoke should return success when repository delete succeeds`() = runTest {
        val paymentMethodId = "pm_123"
        coEvery { repository.deleteVaultedPaymentMethod(paymentMethodId) } returns Result.success(Unit)

        val result = useCase(paymentMethodId)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.deleteVaultedPaymentMethod(paymentMethodId) }
    }

    @Test
    fun `invoke should return failure when repository delete fails`() = runTest {
        val paymentMethodId = "pm_123"
        val exception = RuntimeException("Delete failed")
        coEvery { repository.deleteVaultedPaymentMethod(paymentMethodId) } returns Result.failure(exception)

        val result = useCase(paymentMethodId)

        assertTrue(result.isFailure)
        assertSame(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { repository.deleteVaultedPaymentMethod(paymentMethodId) }
    }

    @Test
    fun `invoke should pass correct payment method id to repository`() = runTest {
        val paymentMethodId = "pm_unique_id_456"
        coEvery { repository.deleteVaultedPaymentMethod(any()) } returns Result.success(Unit)

        useCase(paymentMethodId)

        coVerify(exactly = 1) { repository.deleteVaultedPaymentMethod(paymentMethodId) }
    }
}
