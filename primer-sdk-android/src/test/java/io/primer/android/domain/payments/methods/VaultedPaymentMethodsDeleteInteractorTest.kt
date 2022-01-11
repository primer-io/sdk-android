package io.primer.android.domain.payments.methods

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.domain.payments.methods.models.VaultDeleteParams
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodsRepository
import io.primer.android.logging.Logger
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
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

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    private lateinit var deleteInteractor: VaultedPaymentMethodsDeleteInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        deleteInteractor =
            VaultedPaymentMethodsDeleteInteractor(
                vaultedPaymentMethodsRepository,
                logger,
                testCoroutineDispatcher
            )
    }

    @Test
    fun `execute() should dispatch TokenRemovedFromVault when deleteVaultedPaymentMethod was success`() {
        val params = mockk<VaultDeleteParams>(relaxed = true)
        every { vaultedPaymentMethodsRepository.deleteVaultedPaymentMethod(any()) }.returns(
            flowOf(Unit)
        )
        testCoroutineDispatcher.runBlockingTest {
            deleteInteractor(params).first()
        }

        verify { vaultedPaymentMethodsRepository.deleteVaultedPaymentMethod(any()) }
    }

    @Test
    fun `execute() should dispatch TokenizeError when exchangeVaultedPaymentToken was failed`() {
        val params = mockk<VaultDeleteParams>(relaxed = true)
        every { vaultedPaymentMethodsRepository.deleteVaultedPaymentMethod(any()) }.returns(
            flow { throw Exception("Delete failed.") }
        )
        assertThrows<Exception> {
            testCoroutineDispatcher.runBlockingTest {
                deleteInteractor(params).first()
            }
        }
        val message = slot<String>()

        verify { vaultedPaymentMethodsRepository.deleteVaultedPaymentMethod(any()) }
        verify { logger.error(capture(message)) }

        assertEquals("Delete failed.", message.captured)
    }
}
