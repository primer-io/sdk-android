package io.primer.android.vault.implementation.vaultedMethods.domain

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.components.domain.exception.VaultManagerFetchException
import io.primer.android.core.domain.None
import io.primer.android.vault.InstantExecutorExtension
import io.primer.android.vault.implementation.vaultedMethods.data.model.PaymentMethodVaultTokenInternal
import io.primer.android.vault.implementation.vaultedMethods.data.model.toVaultedPaymentMethod
import io.primer.android.vault.implementation.vaultedMethods.domain.repository.VaultedPaymentMethodsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class FetchVaultedPaymentMethodsInteractorTest {

    @RelaxedMockK
    internal lateinit var vaultedPaymentMethodsRepository: VaultedPaymentMethodsRepository

    private lateinit var interactor: FetchVaultedPaymentMethodsInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor = FetchVaultedPaymentMethodsInteractor(
            vaultedPaymentMethodsRepository
        )
    }

    @Test
    fun `execute() should return list of PrimerVaultedPaymentMethodData when getVaultedPaymentMethods is successful`() {
        val params = mockk<None>(relaxed = true)
        val vaultTokenInternal = mockk<PaymentMethodVaultTokenInternal>(relaxed = true)
        coEvery { vaultedPaymentMethodsRepository.getVaultedPaymentMethods(any()) }.returns(
            Result.success(listOf(vaultTokenInternal))
        )
        runTest {
            val result = interactor(params)

            assertEquals(true, result.isSuccess)
            assertEquals(listOf(vaultTokenInternal.toVaultedPaymentMethod()), result.getOrNull())
        }

        coVerify { vaultedPaymentMethodsRepository.getVaultedPaymentMethods(any()) }
    }

    @Test
    fun `execute() should return correct Exception when getVaultedPaymentMethods failed`() {
        val params = mockk<None>(relaxed = true)
        val mockException = mockk<VaultManagerFetchException>(relaxed = true)
        coEvery { vaultedPaymentMethodsRepository.getVaultedPaymentMethods(any()) }.returns(
            Result.failure(mockException)
        )

        val exception = assertThrows<VaultManagerFetchException> {
            runTest {
                interactor(params).getOrThrow()
            }
        }

        coVerify { vaultedPaymentMethodsRepository.getVaultedPaymentMethods(any()) }

        assertEquals(mockException, exception)
    }
}
