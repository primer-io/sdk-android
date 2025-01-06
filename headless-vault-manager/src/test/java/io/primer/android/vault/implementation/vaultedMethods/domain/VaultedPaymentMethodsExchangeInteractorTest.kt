package io.primer.android.vault.implementation.vaultedMethods.domain

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.payments.core.tokenization.data.model.toPaymentMethodToken
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.vault.InstantExecutorExtension
import io.primer.android.vault.implementation.vaultedMethods.domain.model.VaultTokenParams
import io.primer.android.vault.implementation.vaultedMethods.domain.repository.VaultedPaymentMethodExchangeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
class VaultedPaymentMethodsExchangeInteractorTest {
    @RelaxedMockK
    internal lateinit var vaultedPaymentMethodExchangeRepository: VaultedPaymentMethodExchangeRepository

    @RelaxedMockK
    internal lateinit var tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository

    @RelaxedMockK
    internal lateinit var preTokenizationHandler: PreTokenizationHandler

    @RelaxedMockK
    internal lateinit var logReporter: LogReporter

    private lateinit var interactor: VaultedPaymentMethodsExchangeInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor =
            VaultedPaymentMethodsExchangeInteractor(
                vaultedPaymentMethodExchangeRepository = vaultedPaymentMethodExchangeRepository,
                tokenizedPaymentMethodRepository = tokenizedPaymentMethodRepository,
                preTokenizationHandler = preTokenizationHandler,
                logReporter = logReporter,
            )
    }

    @Test
    fun `execute() should return payment method token event when exchangeVaultedPaymentToken was success`() {
        val params = mockk<VaultTokenParams>(relaxed = true)
        val paymentMethodTokenInternal = mockk<PaymentMethodTokenInternal>(relaxed = true)
        coEvery {
            preTokenizationHandler.handle(any(), any())
        }.returns(Result.success(Unit))

        coEvery {
            vaultedPaymentMethodExchangeRepository.exchangeVaultedPaymentToken(
                any(),
                any(),
            )
        }.returns(Result.success(paymentMethodTokenInternal))

        runTest {
            val result = interactor(params)
            assertEquals(paymentMethodTokenInternal.toPaymentMethodToken(), result.getOrThrow())
        }

        coVerify {
            vaultedPaymentMethodExchangeRepository.exchangeVaultedPaymentToken(
                any(),
                any(),
            )
        }
        verify { tokenizedPaymentMethodRepository.setPaymentMethod(paymentMethodTokenInternal) }
        coVerify { preTokenizationHandler.handle(any(), any()) }
    }

    @Test
    fun `execute() should return error event when exchangeVaultedPaymentToken was failed`() {
        val params = mockk<VaultTokenParams>(relaxed = true)
        val expectedException = mockk<Exception>(relaxed = true)
        coEvery {
            vaultedPaymentMethodExchangeRepository.exchangeVaultedPaymentToken(
                any(),
                any(),
            )
        }.returns(Result.failure(expectedException))

        coEvery {
            preTokenizationHandler.handle(any(), any())
        }.returns(Result.success(Unit))

        val exception =
            assertThrows<Exception> {
                runTest {
                    interactor(params).getOrThrow()
                }
            }

        coVerify {
            vaultedPaymentMethodExchangeRepository.exchangeVaultedPaymentToken(
                any(),
                any(),
            )
        }
        assertEquals(exception.javaClass, expectedException.javaClass)
    }
}
