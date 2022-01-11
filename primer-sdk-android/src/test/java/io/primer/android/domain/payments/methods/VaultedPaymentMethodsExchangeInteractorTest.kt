package io.primer.android.domain.payments.methods

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.completion.ResumeHandlerFactory
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.domain.payments.methods.models.VaultTokenParams
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodsRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.CheckoutEventType
import io.primer.android.events.EventDispatcher
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
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
class VaultedPaymentMethodsExchangeInteractorTest {

    @RelaxedMockK
    internal lateinit var vaultedPaymentMethodsRepository: VaultedPaymentMethodsRepository

    @RelaxedMockK
    internal lateinit var paymentMethodRepository: PaymentMethodRepository

    @RelaxedMockK
    internal lateinit var resumeHandlerFactory: ResumeHandlerFactory

    @RelaxedMockK
    internal lateinit var eventDispatcher: EventDispatcher

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    private lateinit var exchangeInteractor: VaultedPaymentMethodsExchangeInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        exchangeInteractor =
            VaultedPaymentMethodsExchangeInteractor(
                vaultedPaymentMethodsRepository,
                paymentMethodRepository,
                resumeHandlerFactory,
                eventDispatcher,
                testCoroutineDispatcher
            )
    }

    @Test
    fun `execute() should dispatch TokenSelected when exchangeVaultedPaymentToken was success`() {
        val params = mockk<VaultTokenParams>(relaxed = true)
        val paymentMethodTokenInternal = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { vaultedPaymentMethodsRepository.exchangeVaultedPaymentToken(any()) }.returns(
            flowOf(
                paymentMethodTokenInternal
            )
        )
        testCoroutineDispatcher.runBlockingTest {
            exchangeInteractor(params).first()
        }
        val event = slot<CheckoutEvent>()

        verify { vaultedPaymentMethodsRepository.exchangeVaultedPaymentToken(any()) }
        verify { paymentMethodRepository.setPaymentMethod(paymentMethodTokenInternal) }
        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assert(event.captured.type == CheckoutEventType.TOKEN_SELECTED)
    }

    @Test
    fun `execute() should dispatch TokenizeError when exchangeVaultedPaymentToken was failed`() {
        val params = mockk<VaultTokenParams>(relaxed = true)
        every { vaultedPaymentMethodsRepository.exchangeVaultedPaymentToken(any()) }.returns(
            flow { throw Exception("Exchange failed.") }
        )
        assertThrows<Exception> {
            testCoroutineDispatcher.runBlockingTest {
                exchangeInteractor(params).first()
            }
        }
        val event = slot<CheckoutEvent>()

        verify { vaultedPaymentMethodsRepository.exchangeVaultedPaymentToken(any()) }
        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assert(event.captured.type == CheckoutEventType.TOKENIZE_ERROR)
    }
}
