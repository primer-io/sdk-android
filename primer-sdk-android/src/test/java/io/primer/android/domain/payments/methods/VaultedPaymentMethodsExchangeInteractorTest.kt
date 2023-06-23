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
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.domain.error.CheckoutErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.methods.models.VaultTokenParams
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodExchangeRepository
import io.primer.android.domain.tokenization.helpers.PostTokenizationEventResolver
import io.primer.android.domain.tokenization.helpers.PreTokenizationEventsResolver
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
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
class VaultedPaymentMethodsExchangeInteractorTest {

    @RelaxedMockK
    internal lateinit var vaultedPaymentMethodExchangeRepository:
        VaultedPaymentMethodExchangeRepository

    @RelaxedMockK
    internal lateinit var paymentMethodRepository: PaymentMethodRepository

    @RelaxedMockK
    internal lateinit var preTokenizationEventsResolver: PreTokenizationEventsResolver

    @RelaxedMockK
    internal lateinit var postTokenizationEventResolver: PostTokenizationEventResolver

    @RelaxedMockK
    internal lateinit var errorEventResolver: CheckoutErrorEventResolver

    private lateinit var interactor: VaultedPaymentMethodsExchangeInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor = VaultedPaymentMethodsExchangeInteractor(
            vaultedPaymentMethodExchangeRepository,
            paymentMethodRepository,
            preTokenizationEventsResolver,
            postTokenizationEventResolver,
            errorEventResolver,
        )
    }

    @Test
    fun `execute() should dispatch token event when exchangeVaultedPaymentToken was success`() {
        val params = mockk<VaultTokenParams>(relaxed = true)
        val paymentMethodTokenInternal = mockk<PaymentMethodTokenInternal>(relaxed = true)
        coEvery {
            vaultedPaymentMethodExchangeRepository.exchangeVaultedPaymentToken(
                any(),
                any()
            )
        }.returns(
            flowOf(
                paymentMethodTokenInternal
            )
        )
        runTest {
            interactor(params).first()
        }

        val token = slot<PaymentMethodTokenInternal>()

        coVerify {
            vaultedPaymentMethodExchangeRepository.exchangeVaultedPaymentToken(
                any(),
                any()
            )
        }
        verify { paymentMethodRepository.setPaymentMethod(paymentMethodTokenInternal) }
        verify { postTokenizationEventResolver.resolve(capture(token)) }

        assertEquals(paymentMethodTokenInternal, token.captured)
    }

    @Test
    fun `execute() should dispatch error event when exchangeVaultedPaymentToken was failed`() {
        val params = mockk<VaultTokenParams>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)
        coEvery {
            vaultedPaymentMethodExchangeRepository.exchangeVaultedPaymentToken(
                any(),
                any()
            )
        }.returns(
            flow { throw exception }
        )
        assertThrows<Exception> {
            runTest {
                interactor(params).first()
            }
        }
        val event = slot<Exception>()

        coVerify {
            vaultedPaymentMethodExchangeRepository.exchangeVaultedPaymentToken(
                any(),
                any()
            )
        }
        verify { errorEventResolver.resolve(capture(event), ErrorMapperType.DEFAULT) }
        assertEquals(exception.javaClass, event.captured.javaClass)
    }
}
