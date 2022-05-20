package io.primer.android.domain.tokenization.helpers

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.PrimerPaymentMethodIntent
import io.primer.android.completion.ResumeHandlerFactory
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.CheckoutEventType
import io.primer.android.events.EventDispatcher
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.settings.internal.PrimerIntent
import io.primer.android.data.tokenization.models.TokenType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class PostTokenizationEventResolverTest {

    @RelaxedMockK
    internal lateinit var config: PrimerConfig

    @RelaxedMockK
    internal lateinit var resumeHandlerFactory: ResumeHandlerFactory

    @RelaxedMockK
    internal lateinit var eventDispatcher: EventDispatcher

    private lateinit var tokenizationEventsResolver: PostTokenizationEventResolver

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        tokenizationEventsResolver =
            PostTokenizationEventResolver(
                config,
                resumeHandlerFactory,
                eventDispatcher,
            )
    }

    @Test
    fun `resolve() should dispatch TOKENIZE_SUCCESS type when payment intent is VAULT and tokenType is SINGLE_USE`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { config.intent }.returns(PrimerIntent(PrimerPaymentMethodIntent.VAULT))

        runTest {
            tokenizationEventsResolver.resolve(paymentMethodToken)
        }

        val event = slot<List<CheckoutEvent>>()

        verify { eventDispatcher.dispatchEvents(capture(event)) }

        assertEquals(CheckoutEventType.TOKENIZE_SUCCESS, event.captured[0].type)
    }

    @Test
    fun `resolve() should dispatch TOKENIZE_SUCCESS, TOKEN_ADDED_TO_VAULT type when payment intent is VAULT and tokenType is MULTI_USE`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { paymentMethodToken.tokenType }.returns(TokenType.MULTI_USE)
        every { config.intent }.returns(PrimerIntent(PrimerPaymentMethodIntent.VAULT))

        runTest {
            tokenizationEventsResolver.resolve(paymentMethodToken)
        }

        val event = slot<List<CheckoutEvent>>()

        verify { eventDispatcher.dispatchEvents(capture(event)) }

        assertEquals(CheckoutEventType.TOKENIZE_SUCCESS, event.captured[0].type)
        assertEquals(CheckoutEventType.TOKEN_ADDED_TO_VAULT, event.captured[1].type)
    }

    @Test
    fun `resolve() should dispatch TOKENIZE_SUCCESS type when payment handling is MANUAL and tokenType is SINGLE_USE`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { config.settings.paymentHandling }.returns(PrimerPaymentHandling.MANUAL)

        runTest {
            tokenizationEventsResolver.resolve(paymentMethodToken)
        }

        val event = slot<List<CheckoutEvent>>()

        verify { eventDispatcher.dispatchEvents(capture(event)) }

        assertEquals(CheckoutEventType.TOKENIZE_SUCCESS, event.captured[0].type)
    }

    @Test
    fun `resolve() should dispatch TOKENIZE_SUCCESS, TOKEN_ADDED_TO_VAULT type when payment handling is MANUAL tokenType is MULTI_USE`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { paymentMethodToken.tokenType }.returns(TokenType.MULTI_USE)
        every { config.settings.paymentHandling }.returns(PrimerPaymentHandling.MANUAL)

        runTest {
            tokenizationEventsResolver.resolve(paymentMethodToken)
        }

        val event = slot<List<CheckoutEvent>>()

        verify { eventDispatcher.dispatchEvents(capture(event)) }

        assertEquals(CheckoutEventType.TOKENIZE_SUCCESS, event.captured[0].type)
        assertEquals(CheckoutEventType.TOKEN_ADDED_TO_VAULT, event.captured[1].type)
    }

    @Test
    fun `resolve() should dispatch PAYMENT_CONTINUE_HUC type when payment handling is AUTO and fromHUC`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { config.settings.paymentHandling }.returns(PrimerPaymentHandling.AUTO)
        every { config.settings.fromHUC }.returns(true)

        runTest {
            tokenizationEventsResolver.resolve(paymentMethodToken)
        }

        val event = slot<List<CheckoutEvent>>()

        verify { eventDispatcher.dispatchEvents(capture(event)) }

        assertEquals(CheckoutEventType.PAYMENT_CONTINUE_HUC, event.captured[0].type)
    }

    @Test
    fun `resolve() should dispatch PAYMENT_CONTINUE type when payment handling is AUTO and not fromHUC`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { config.settings.paymentHandling }.returns(PrimerPaymentHandling.AUTO)
        every { config.settings.fromHUC }.returns(false)

        runTest {
            tokenizationEventsResolver.resolve(paymentMethodToken)
        }

        val event = slot<List<CheckoutEvent>>()

        verify { eventDispatcher.dispatchEvents(capture(event)) }

        assertEquals(CheckoutEventType.PAYMENT_CONTINUE, event.captured[0].type)
    }
}
