package io.primer.android.domain.payments.methods

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.domain.payments.methods.models.VaultDeleteParams
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodsRepository
import io.primer.android.logging.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class VaultedPaymentMethodsDeleteInteractorTest {

    @RelaxedMockK
    internal lateinit var vaultedPaymentMethodsRepository: VaultedPaymentMethodsRepository

    @RelaxedMockK
    internal lateinit var logger: Logger

    private lateinit var interactor: VaultedPaymentMethodsDeleteInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor = VaultedPaymentMethodsDeleteInteractor(
            vaultedPaymentMethodsRepository,
            logger,
        )
    }

    @Test
    fun `execute() should dispatch TokenRemovedFromVault when deleteVaultedPaymentMethod was success`() {
        val params = mockk<VaultDeleteParams>(relaxed = true)
        coEvery { vaultedPaymentMethodsRepository.deleteVaultedPaymentMethod(any()) }.returns(
            flowOf(Unit)
        )
        runTest {
            interactor(params).first()
        }

        coVerify { vaultedPaymentMethodsRepository.deleteVaultedPaymentMethod(any()) }
    }

    @Test
    fun `execute() should dispatch TokenizeError when exchangeVaultedPaymentToken was failed`() {
        val params = mockk<VaultDeleteParams>(relaxed = true)
        coEvery { vaultedPaymentMethodsRepository.deleteVaultedPaymentMethod(any()) }.returns(
            flow { throw Exception("Delete failed.") }
        )
        assertThrows<Exception> {
            runTest {
                interactor(params).first()
            }
        }
        val message = slot<String>()

        coVerify { vaultedPaymentMethodsRepository.deleteVaultedPaymentMethod(any()) }
        verify { logger.error(capture(message)) }

        assertEquals("Delete failed.", message.captured)
    }
}
