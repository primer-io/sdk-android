package io.primer.android.internal.domain.usecase.vault

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.internal.domain.repositories.HeadlessRepository
import io.primer.android.internal.domain.repositories.PrimerVaultManagerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubmitVaultedPaymentUseCaseTest {

    private lateinit var repository: PrimerVaultManagerRepository
    private lateinit var headlessRepository: HeadlessRepository
    private lateinit var useCase: SubmitVaultedPaymentUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk()
        headlessRepository = mockk()
        useCase = SubmitVaultedPaymentUseCase(repository, headlessRepository)
    }

    @Test
    fun `invoke should start payment flow without cvv and return checkout data`() = runTest {
        val mockCheckoutData = mockk<PrimerCheckoutData>()
        coEvery { repository.startPaymentFlow("vault-id", null) } returns Result.success(Unit)
        coEvery { headlessRepository.awaitPaymentResult() } returns Result.success(mockCheckoutData)

        val result = useCase("vault-id")

        assertTrue(result.isSuccess)
        assertSame(mockCheckoutData, result.getOrNull())
        coVerify(exactly = 1) { repository.startPaymentFlow("vault-id", null) }
        coVerify(exactly = 1) { headlessRepository.awaitPaymentResult() }
    }

    @Test
    fun `invoke should start payment flow with cvv when provided`() = runTest {
        val mockCheckoutData = mockk<PrimerCheckoutData>()
        coEvery {
            repository
                .startPaymentFlow("vault-id", any<PrimerVaultedCardAdditionalData>())
        } returns Result.success(Unit)
        coEvery { headlessRepository.awaitPaymentResult() } returns Result.success(mockCheckoutData)

        val result = useCase("vault-id", cvv = "123")

        assertTrue(result.isSuccess)
        assertSame(mockCheckoutData, result.getOrNull())
        coVerify(exactly = 1) {
            repository.startPaymentFlow("vault-id", match<PrimerVaultedCardAdditionalData> { it.cvv == "123" })
        }
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        val expected = IllegalStateException("Unable to start")
        coEvery { repository.startPaymentFlow("vault-id", null) } returns Result.failure(expected)

        val result = useCase("vault-id")

        assertTrue(result.isFailure)
        assertSame(expected, result.exceptionOrNull())
        coVerify(exactly = 0) { headlessRepository.awaitPaymentResult() }
    }

    @Test
    fun `invoke should return failure when headless repository fails`() = runTest {
        val expected = IllegalStateException("Payment failed")
        coEvery { repository.startPaymentFlow("vault-id", null) } returns Result.success(Unit)
        coEvery { headlessRepository.awaitPaymentResult() } returns Result.failure(expected)

        val result = useCase("vault-id")

        assertTrue(result.isFailure)
        assertSame(expected, result.exceptionOrNull())
    }
}
