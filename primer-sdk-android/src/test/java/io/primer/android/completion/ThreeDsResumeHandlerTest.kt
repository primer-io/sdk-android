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
class ThreeDsResumeHandlerTest {

    @RelaxedMockK
    internal lateinit var clientTokenRepository: ClientTokenRepository

    @RelaxedMockK
    internal lateinit var paymentMethodRepository: PaymentMethodRepository

    @RelaxedMockK
    internal lateinit var eventDispatcher: EventDispatcher

    @RelaxedMockK
    internal lateinit var logger: Logger

    private lateinit var resumeHandler: ThreeDsResumeHandler

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        resumeHandler =
            ThreeDsResumeHandler(
                clientTokenRepository,
                paymentMethodRepository,
                eventDispatcher,
                logger
            )
    }

    @Test
    fun `handleNewClientToken() should dispatch Start3DS event when ClientTokenIntent is 3DS_AUTHENTICATION`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { clientTokenRepository.getClientTokenIntent() }.returns(ClientTokenIntent.`3DS_AUTHENTICATION`)
        every { paymentMethodToken.paymentInstrumentType }.returns(PAYMENT_CARD_IDENTIFIER)
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)

        runBlockingTest {
            resumeHandler.handleNewClientToken("")
        }

        val event = slot<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assert(event.captured.type == CheckoutEventType.START_3DS)
    }

    @Test
    fun `handleNewClientToken() should dispatch resume error event when ClientTokenIntent is not 3DS_AUTHENTICATION`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { paymentMethodToken.paymentInstrumentType }.returns(PAYMENT_CARD_IDENTIFIER)
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)

        runBlockingTest {
            resumeHandler.handleNewClientToken("")
        }

        val event = slot<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assert(event.captured.type == CheckoutEventType.RESUME_ERR0R)
    }

    @Test
    fun `handleNewClientToken() should dispatch resume error event when ClientTokenIntent is 3DS_AUTHENTICATION and paymentMethodInstrumentType is not PAYMENT_CARD`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { clientTokenRepository.getClientTokenIntent() }.returns(ClientTokenIntent.`3DS_AUTHENTICATION`)
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)

        runBlockingTest {
            resumeHandler.handleNewClientToken("")
        }

        val event = slot<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assert(event.captured.type == CheckoutEventType.RESUME_ERR0R)
    }

    private companion object {
        const val PAYMENT_CARD_IDENTIFIER = "PAYMENT_CARD"
    }
}
