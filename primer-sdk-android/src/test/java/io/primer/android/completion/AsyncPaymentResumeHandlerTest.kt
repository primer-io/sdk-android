package io.primer.android.completion

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.CheckoutEventType
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.model.dto.PaymentMethodTokenInternal
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
class AsyncPaymentResumeHandlerTest {

    @RelaxedMockK
    internal lateinit var clientTokenRepository: ClientTokenRepository

    @RelaxedMockK
    internal lateinit var paymentMethodRepository: PaymentMethodRepository

    @RelaxedMockK
    internal lateinit var eventDispatcher: EventDispatcher

    @RelaxedMockK
    internal lateinit var logger: Logger

    private lateinit var resumeHandler: AsyncPaymentResumeHandler

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        resumeHandler =
            AsyncPaymentResumeHandler(
                clientTokenRepository,
                paymentMethodRepository,
                eventDispatcher,
                logger
            )
    }

    @Test
    fun `handleNewClientToken() should dispatch StartAsyncFlow event when ClientTokenIntent is PAY_NL_IDEAL_REDIRECTION and paymentMethodType is PAY_NL_IDEAL_IDENTIFIER`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.PAY_NL_IDEAL_REDIRECTION
        )
        every { paymentMethodToken.paymentInstrumentData?.paymentMethodType }.returns(
            PAY_NL_IDEAL_IDENTIFIER
        )
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)

        runBlockingTest {
            resumeHandler.handleNewClientToken("")
        }

        val event = slot<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assert(event.captured.type == CheckoutEventType.START_ASYNC_FLOW)
    }

    @Test
    fun `handleNewClientToken() should dispatch StartAsyncFlow event when ClientTokenIntent is HOOLAH_REDIRECTION and paymentMethodType is HOOLAH_IDENTIFIER`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.HOOLAH_REDIRECTION
        )
        every { paymentMethodToken.paymentInstrumentData?.paymentMethodType }.returns(
            HOOLAH_IDENTIFIER
        )
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)

        runBlockingTest {
            resumeHandler.handleNewClientToken("")
        }

        val event = slot<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assert(event.captured.type == CheckoutEventType.START_ASYNC_FLOW)
    }

    @Test
    fun `handleNewClientToken() should dispatch resume error event when ClientTokenIntent is not PAY_NL_IDEAL_REDIRECTION`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)

        runBlockingTest {
            resumeHandler.handleNewClientToken("")
        }

        val event = slot<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assert(event.captured.type == CheckoutEventType.RESUME_ERR0R)
    }

    @Test
    fun `handleNewClientToken() should dispatch resume error event when ClientTokenIntent is not HOOLAH_REDIRECTION`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)

        runBlockingTest {
            resumeHandler.handleNewClientToken("")
        }

        val event = slot<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assert(event.captured.type == CheckoutEventType.RESUME_ERR0R)
    }

    @Test
    fun `handleNewClientToken() should dispatch resume error event when ClientTokenIntent is PAY_NL_IDEAL_REDIRECTION and paymentMethodInstrumentType is not PAY_NL_IDEAL_IDENTIFIER`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.PAY_NL_IDEAL_REDIRECTION
        )
        every { paymentMethodToken.paymentInstrumentData?.paymentMethodType }.returns(
            HOOLAH_IDENTIFIER
        )
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)

        runBlockingTest {
            resumeHandler.handleNewClientToken("")
        }

        val event = slot<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assert(event.captured.type == CheckoutEventType.RESUME_ERR0R)
    }

    @Test
    fun `handleNewClientToken() should dispatch resume error event when ClientTokenIntent is HOOLAH_REDIRECTION and paymentMethodInstrumentType is not HOOLAH_IDENTIFIER`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.HOOLAH_REDIRECTION
        )
        every { paymentMethodToken.paymentInstrumentData?.paymentMethodType }.returns(
            PAY_NL_IDEAL_IDENTIFIER
        )
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)

        runBlockingTest {
            resumeHandler.handleNewClientToken("")
        }

        val event = slot<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assert(event.captured.type == CheckoutEventType.RESUME_ERR0R)
    }

    private companion object {

        const val PAY_NL_IDEAL_IDENTIFIER = "PAY_NL_IDEAL"
        const val HOOLAH_IDENTIFIER = "HOOLAH"
    }
}
