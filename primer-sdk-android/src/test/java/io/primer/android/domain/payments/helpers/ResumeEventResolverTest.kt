package io.primer.android.domain.payments.helpers

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
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.CheckoutEventType
import io.primer.android.events.EventDispatcher
import io.primer.android.model.dto.PaymentHandling
import io.primer.android.model.dto.PrimerConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class ResumeEventResolverTest {

    @RelaxedMockK
    internal lateinit var config: PrimerConfig

    @RelaxedMockK
    internal lateinit var resumeHandlerFactory: ResumeHandlerFactory

    @RelaxedMockK
    internal lateinit var eventDispatcher: EventDispatcher

    private lateinit var resumeEventResolver: ResumeEventResolver

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        resumeEventResolver =
            ResumeEventResolver(
                config,
                resumeHandlerFactory,
                eventDispatcher,
            )
    }

    @Test
    fun `resolve() should dispatch RESUME_SUCCESS_INTERNAL type when payment handling is AUTO`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { config.settings.options.paymentHandling }.returns(PaymentHandling.AUTO)

        runBlockingTest {
            resumeEventResolver.resolve(paymentMethodToken.paymentInstrumentType)
        }
        val event = slot<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assertEquals(CheckoutEventType.RESUME_SUCCESS_INTERNAL, event.captured.type)
    }

    @Test
    fun `resolve() should dispatch RESUME_SUCCESS type when payment handling is MANUAL`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { config.settings.options.paymentHandling }.returns(PaymentHandling.MANUAL)

        runBlockingTest {
            resumeEventResolver.resolve(paymentMethodToken.paymentInstrumentType)
        }
        val event = slot<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assertEquals(CheckoutEventType.RESUME_SUCCESS, event.captured.type)
    }
}
