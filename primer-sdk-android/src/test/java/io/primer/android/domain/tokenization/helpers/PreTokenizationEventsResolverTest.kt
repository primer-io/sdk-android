package io.primer.android.domain.tokenization.helpers

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.PrimerSessionIntent
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.settings.internal.PrimerIntent
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.CheckoutEventType
import io.primer.android.events.EventDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
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
                eventDispatcher
            )
    }

    @Test
    fun `resolve() should dispatch TOKENIZE_STARTED type when payment intent is VAULT`() {
        val paymentMethodType = mockk<PaymentMethodType>(relaxed = true)

        every { config.intent }.returns(PrimerIntent(PrimerSessionIntent.VAULT))

        runTest {
            tokenizationEventsResolver.resolve(paymentMethodType.name)
        }

        val events = mutableListOf<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(events)) }

        assertTrue(events.any { it.type == CheckoutEventType.TOKENIZE_STARTED })
        assertTrue(events.any { it.type == CheckoutEventType.DISABLE_DIALOG_DISMISS })
    }

    @Test
    fun `resolve() should dispatch TOKENIZE_STARTED and DISABLE_DIALOG_DISMISS type when payment handling is MANUAL`() {
        val paymentMethodType = mockk<PaymentMethodType>(relaxed = true)

        every { config.settings.paymentHandling }.returns(PrimerPaymentHandling.MANUAL)

        runTest {
            tokenizationEventsResolver.resolve(paymentMethodType.name)
        }

        val events = mutableListOf<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(events)) }

        assertTrue(events.any { it.type == CheckoutEventType.TOKENIZE_STARTED })
        assertTrue(events.any { it.type == CheckoutEventType.DISABLE_DIALOG_DISMISS })
    }
}
