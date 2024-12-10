package io.primer.android.vault.implementation.vaultedMethods.domain

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.vault.InstantExecutorExtension
import io.primer.android.vault.implementation.vaultedMethods.domain.model.VaultDeleteParams
import io.primer.android.vault.implementation.vaultedMethods.domain.repository.VaultedPaymentMethodsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class VaultedPaymentMethodsDeleteInteractorTest {

    @RelaxedMockK
    internal lateinit var vaultedPaymentMethodsRepository: VaultedPaymentMethodsRepository

    @RelaxedMockK
    internal lateinit var logReporter: LogReporter

    private lateinit var interactor: VaultedPaymentMethodsDeleteInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor = VaultedPaymentMethodsDeleteInteractor(
            vaultedPaymentMethodsRepository,
            logReporter
        )
    }

    @Test
    fun `execute() should return success when deleteVaultedPaymentMethod was success`() {
        val params = mockk<VaultDeleteParams>(relaxed = true)
        coEvery { vaultedPaymentMethodsRepository.deleteVaultedPaymentMethod(any()) }.returns(
            Result.success(Unit)
        )
        runTest {
            interactor(params)
        }

        coVerify { vaultedPaymentMethodsRepository.deleteVaultedPaymentMethod(any()) }
    }

    @Test
    fun `execute() should return failure when deleteVaultedPaymentMethod was failed`() {
        val params = mockk<VaultDeleteParams>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message } returns "Delete failed."
        coEvery { vaultedPaymentMethodsRepository.deleteVaultedPaymentMethod(any()) }.returns(
            Result.failure(exception)
        )
        runTest {
            interactor(params)
        }

        val message = slot<String>()
        val throwable = slot<Throwable>()

        coVerify { vaultedPaymentMethodsRepository.deleteVaultedPaymentMethod(any()) }
        verify { logReporter.error(capture(message), throwable = capture(throwable)) }

        assertEquals("Delete failed.", message.captured)
        assertEquals(exception, throwable.captured)
    }
}
