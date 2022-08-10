package io.primer.android.domain.tokenization.helpers

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.PrimerSessionIntent
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.CheckoutEventType
import io.primer.android.events.EventDispatcher
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.settings.internal.PrimerIntent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class PreTokenizationEventsResolverTest {

    @RelaxedMockK
    internal lateinit var config: PrimerConfig

    @RelaxedMockK
    internal lateinit var eventDispatcher: EventDispatcher

    private lateinit var tokenizationEventsResolver: PreTokenizationEventsResolver

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        tokenizationEventsResolver =
            PreTokenizationEventsResolver(
                config,
                eventDispatcher,
            )
    }

    @Test
    fun `resolve() should dispatch TOKENIZE_STARTED type when payment intent is VAULT`() {
        val paymentMethodType = mockk<PaymentMethodType>(relaxed = true)

        every { config.intent }.returns(PrimerIntent(PrimerSessionIntent.VAULT))

        runTest {
            tokenizationEventsResolver.resolve(paymentMethodType.name)
        }

        val event = slot<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assertEquals(CheckoutEventType.TOKENIZE_STARTED, event.captured.type)
    }

    @Test
    fun `resolve() should dispatch TOKENIZE_STARTED type when payment handling is MANUAL`() {
        val paymentMethodType = mockk<PaymentMethodType>(relaxed = true)

        every { config.settings.paymentHandling }.returns(PrimerPaymentHandling.MANUAL)

        runTest {
            tokenizationEventsResolver.resolve(paymentMethodType.name)
        }

        val event = slot<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assertEquals(CheckoutEventType.TOKENIZE_STARTED, event.captured.type)
    }
}
