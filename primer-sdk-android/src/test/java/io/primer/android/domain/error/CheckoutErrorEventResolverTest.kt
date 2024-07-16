package io.primer.android.domain.error

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.error.models.PaymentMethodError
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.CheckoutEventType
import io.primer.android.events.EventDispatcher
import io.primer.android.ui.fragments.ErrorType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class CheckoutErrorEventResolverTest {

    private lateinit var checkoutErrorEventResolver: CheckoutErrorEventResolver

    @RelaxedMockK
    internal lateinit var analyticsRepository: AnalyticsRepository

    @RelaxedMockK
    internal lateinit var errorMapperFactory: ErrorMapperFactory

    @RelaxedMockK
    internal lateinit var settings: PrimerSettings

    @RelaxedMockK
    internal lateinit var logReporter: LogReporter

    @RelaxedMockK
    internal lateinit var eventDispatcher: EventDispatcher

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        checkoutErrorEventResolver = CheckoutErrorEventResolver(
            analyticsRepository,
            errorMapperFactory,
            logReporter,
            settings,
            eventDispatcher
        )
    }

    @Test
    fun `resolve() should dispatch CHECKOUT_AUTO_ERROR type when payment handling is AUTO`() {
        val mapperType = mockk<ErrorMapperType>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)
        every { errorMapperFactory.buildErrorMapper(any()) } returns mockk {
            every { getPrimerError(any()) } returns PaymentMethodError.PaymentMethodCancelledError("")
        }
        every { exception.message }.returns("Something went wrong.")

        runTest {
            checkoutErrorEventResolver.resolve(exception, mapperType)
        }

        val events = mutableListOf<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(events)) }
        assertEquals(CheckoutEventType.CHECKOUT_AUTO_ERROR, events.last().type)
        (events.last() as CheckoutEvent.CheckoutPaymentError).errorHandler?.showErrorMessage("error")
        verify { eventDispatcher.dispatchEvent(capture(events)) }
        assertEquals(ErrorType.PAYMENT_CANCELLED, (events.last() as CheckoutEvent.ShowError).errorType)
    }

    @Test
    fun `resolve() should dispatch CHECKOUT_MANUAL_ERROR type when payment handling is MANUAL`() {
        val mapperType = mockk<ErrorMapperType>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)
        every { errorMapperFactory.buildErrorMapper(any()) } returns mockk {
            every { getPrimerError(any()) } returns PaymentMethodError.UnsupportedPaymentMethodError("")
        }
        every { exception.message }.returns("Something went wrong.")
        every { settings.paymentHandling }.returns(PrimerPaymentHandling.MANUAL)

        runTest {
            checkoutErrorEventResolver.resolve(exception, mapperType)
        }

        val events = mutableListOf<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(events)) }
        assertEquals(CheckoutEventType.CHECKOUT_MANUAL_ERROR, events.last().type)
        (events.last() as CheckoutEvent.CheckoutError).errorHandler?.showErrorMessage("error")
        verify { eventDispatcher.dispatchEvent(capture(events)) }
        assertEquals(ErrorType.PAYMENT_FAILED, (events.last() as CheckoutEvent.ShowError).errorType)
    }
}
