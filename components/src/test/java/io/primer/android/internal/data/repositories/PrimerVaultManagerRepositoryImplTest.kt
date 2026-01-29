package io.primer.android.internal.data.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.components.manager.vault.PrimerHeadlessUniversalCheckoutVaultManagerInterface
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrimerVaultManagerRepositoryImplTest {

    private lateinit var manager: PrimerHeadlessUniversalCheckoutVaultManagerInterface
    private lateinit var repository: PrimerVaultManagerRepositoryImpl

    @BeforeEach
    fun setUp() {
        manager = mockk()
        repository = PrimerVaultManagerRepositoryImpl(manager)
    }

    @Nested
    inner class FetchVaultedPaymentMethodsTests {

        @Test
        fun `should return success with payment methods when manager succeeds`() = runTest {
            val expectedMethods = listOf(mockk<PrimerVaultedPaymentMethod>())
            coEvery { manager.fetchVaultedPaymentMethods() } returns Result.success(expectedMethods)

            val result = repository.fetchVaultedPaymentMethods()

            assertTrue(result.isSuccess)
            assertEquals(expectedMethods, result.getOrNull())
            coVerify(exactly = 1) { manager.fetchVaultedPaymentMethods() }
        }

        @Test
        fun `should return failure when manager fails`() = runTest {
            val exception = RuntimeException("Fetch failed")
            coEvery { manager.fetchVaultedPaymentMethods() } returns Result.failure(exception)

            val result = repository.fetchVaultedPaymentMethods()

            assertTrue(result.isFailure)
            assertSame(exception, result.exceptionOrNull())
        }

        @Test
        fun `should return empty list when manager returns empty list`() = runTest {
            coEvery { manager.fetchVaultedPaymentMethods() } returns Result.success(emptyList())

            val result = repository.fetchVaultedPaymentMethods()

            assertTrue(result.isSuccess)
            assertTrue(result.getOrNull()?.isEmpty() == true)
        }
    }

    @Nested
    inner class ValidateTests {

        @Test
        fun `should return success with empty errors when validation passes`() = runTest {
            val paymentMethodId = "pm_123"
            val additionalData = PrimerVaultedCardAdditionalData(cvv = "123")
            coEvery {
                manager.validate(paymentMethodId, additionalData)
            } returns Result.success(emptyList())

            val result = repository.validate(paymentMethodId, additionalData)

            assertTrue(result.isSuccess)
            assertTrue(result.getOrNull()?.isEmpty() == true)
            coVerify(exactly = 1) { manager.validate(paymentMethodId, additionalData) }
        }

        @Test
        fun `should return success with validation errors when validation fails`() = runTest {
            val paymentMethodId = "pm_123"
            val additionalData = PrimerVaultedCardAdditionalData(cvv = "12")
            val validationErrors = listOf(
                PrimerValidationError(errorId = "cvv_invalid", description = "CVV is invalid"),
            )
            coEvery {
                manager.validate(paymentMethodId, additionalData)
            } returns Result.success(validationErrors)

            val result = repository.validate(paymentMethodId, additionalData)

            assertTrue(result.isSuccess)
            assertEquals(validationErrors, result.getOrNull())
        }

        @Test
        fun `should return failure when manager throws`() = runTest {
            val paymentMethodId = "pm_123"
            val additionalData = PrimerVaultedCardAdditionalData(cvv = "123")
            val exception = RuntimeException("Validation error")
            coEvery {
                manager.validate(paymentMethodId, additionalData)
            } returns Result.failure(exception)

            val result = repository.validate(paymentMethodId, additionalData)

            assertTrue(result.isFailure)
            assertSame(exception, result.exceptionOrNull())
        }
    }

    @Nested
    inner class StartPaymentFlowTests {

        @Test
        fun `should call manager with additional data when provided`() = runTest {
            val paymentMethodId = "pm_123"
            val additionalData = PrimerVaultedCardAdditionalData(cvv = "123")
            coEvery {
                manager.startPaymentFlow(paymentMethodId, additionalData)
            } returns Result.success(Unit)

            val result = repository.startPaymentFlow(paymentMethodId, additionalData)

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { manager.startPaymentFlow(paymentMethodId, additionalData) }
            coVerify(exactly = 0) { manager.startPaymentFlow(paymentMethodId) }
        }

        @Test
        fun `should call manager without additional data when null`() = runTest {
            val paymentMethodId = "pm_123"
            coEvery { manager.startPaymentFlow(paymentMethodId) } returns Result.success(Unit)

            val result = repository.startPaymentFlow(paymentMethodId, null)

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { manager.startPaymentFlow(paymentMethodId) }
        }

        @Test
        fun `should return failure when manager fails with additional data`() = runTest {
            val paymentMethodId = "pm_123"
            val additionalData = PrimerVaultedCardAdditionalData(cvv = "123")
            val exception = RuntimeException("Payment flow failed")
            coEvery {
                manager.startPaymentFlow(paymentMethodId, additionalData)
            } returns Result.failure(exception)

            val result = repository.startPaymentFlow(paymentMethodId, additionalData)

            assertTrue(result.isFailure)
            assertSame(exception, result.exceptionOrNull())
        }

        @Test
        fun `should return failure when manager fails without additional data`() = runTest {
            val paymentMethodId = "pm_123"
            val exception = RuntimeException("Payment flow failed")
            coEvery { manager.startPaymentFlow(paymentMethodId) } returns Result.failure(exception)

            val result = repository.startPaymentFlow(paymentMethodId, null)

            assertTrue(result.isFailure)
            assertSame(exception, result.exceptionOrNull())
        }
    }

    @Nested
    inner class DeleteVaultedPaymentMethodTests {

        @Test
        fun `should return success when manager deletes successfully`() = runTest {
            val paymentMethodId = "pm_123"
            coEvery { manager.deleteVaultedPaymentMethod(paymentMethodId) } returns Result.success(Unit)

            val result = repository.deleteVaultedPaymentMethod(paymentMethodId)

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { manager.deleteVaultedPaymentMethod(paymentMethodId) }
        }

        @Test
        fun `should return failure when manager fails to delete`() = runTest {
            val paymentMethodId = "pm_123"
            val exception = RuntimeException("Delete failed")
            coEvery { manager.deleteVaultedPaymentMethod(paymentMethodId) } returns Result.failure(exception)

            val result = repository.deleteVaultedPaymentMethod(paymentMethodId)

            assertTrue(result.isFailure)
            assertSame(exception, result.exceptionOrNull())
        }
    }
}
