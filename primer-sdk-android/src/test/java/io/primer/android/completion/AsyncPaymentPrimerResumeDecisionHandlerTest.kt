package io.primer.android.completion

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.domain.deeplink.async.repository.AsyncPaymentMethodDeeplinkRepository
import io.primer.android.domain.error.CheckoutErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.domain.payments.methods.repository.PaymentMethodsRepository
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.domain.token.repository.ValidateTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.CheckoutEventType
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
class AsyncPaymentPrimerResumeDecisionHandlerTest {

    @JvmField
    @RegisterExtension
    internal val instantExecutorExtension = InstantExecutorExtension()

    @RelaxedMockK
    internal lateinit var clientTokenRepository: ClientTokenRepository

    @RelaxedMockK
    internal lateinit var paymentMethodRepository: PaymentMethodRepository

    @RelaxedMockK
    internal lateinit var paymentMethodsRepository: PaymentMethodsRepository

    @RelaxedMockK
    internal lateinit var paymentResultRepository: PaymentResultRepository

    @RelaxedMockK
    internal lateinit var analyticsRepository: AnalyticsRepository

    @RelaxedMockK
    internal lateinit var verificationTokenRepository: ValidateTokenRepository

    @RelaxedMockK
    internal lateinit var errorEventResolver: CheckoutErrorEventResolver

    @RelaxedMockK
    internal lateinit var asyncPaymentMethodDeeplinkRepository: AsyncPaymentMethodDeeplinkRepository

    @RelaxedMockK
    internal lateinit var retailOutletRepository: RetailOutletRepository

    @RelaxedMockK
    internal lateinit var eventDispatcher: EventDispatcher

    @RelaxedMockK
    internal lateinit var config: PrimerConfig

    @RelaxedMockK
    internal lateinit var logger: Logger

    private lateinit var resumeHandler: AsyncPaymentPrimerResumeDecisionHandler

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        resumeHandler =
            AsyncPaymentPrimerResumeDecisionHandler(
                verificationTokenRepository,
                clientTokenRepository,
                paymentMethodRepository,
                paymentResultRepository,
                analyticsRepository,
                errorEventResolver,
                eventDispatcher,
                logger,
                config,
                paymentMethodsRepository,
                retailOutletRepository,
                asyncPaymentMethodDeeplinkRepository,
                instantExecutorExtension.dispatcher
            )
    }

    @Test
    fun `handleNewClientToken() should dispatch StartAsyncRedirectFlow event when ClientTokenIntent is PAY_NL_IDEAL_REDIRECTION and paymentMethodType is PAY_NL_IDEAL`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { verificationTokenRepository.validate(any()) }.returns(
            flowOf(true)
        )
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.PAY_NL_IDEAL_REDIRECTION.name
        )
        every { paymentMethodToken.paymentInstrumentData?.paymentMethodType }.returns(
            PaymentMethodType.PAY_NL_IDEAL.name
        )
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)

        runTest {
            resumeHandler.continueWithNewClientToken("")
        }

        val events = slot<List<CheckoutEvent>>()

        verify { eventDispatcher.dispatchEvents(capture(events)) }

        assertEquals(CheckoutEventType.PAYMENT_METHOD_PRESENTED, events.captured[0].type)
        assertEquals(CheckoutEventType.START_ASYNC_REDIRECT_FLOW, events.captured[1].type)
    }

    @Test
    fun `handleNewClientToken() should dispatch StartAsyncRedirectFlow event when ClientTokenIntent is HOOLAH_REDIRECTION and paymentMethodType is HOOLAH`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { verificationTokenRepository.validate(any()) }.returns(
            flowOf(true)
        )
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.HOOLAH_REDIRECTION.name
        )
        every { paymentMethodToken.paymentInstrumentData?.paymentMethodType }.returns(
            PaymentMethodType.HOOLAH.name
        )
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)

        runTest {
            resumeHandler.continueWithNewClientToken("")
        }

        val events = slot<List<CheckoutEvent>>()

        verify { eventDispatcher.dispatchEvents(capture(events)) }

        assertEquals(CheckoutEventType.PAYMENT_METHOD_PRESENTED, events.captured[0].type)
        assertEquals(CheckoutEventType.START_ASYNC_REDIRECT_FLOW, events.captured[1].type)
    }

    @Test
    fun `handleNewClientToken() should dispatch resume error event when ClientTokenIntent is not PAY_NL_IDEAL_REDIRECTION`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { verificationTokenRepository.validate(any()) }.returns(
            flowOf(true)
        )
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)

        runTest {
            resumeHandler.continueWithNewClientToken("")
        }

        val event = slot<Throwable>()

        verify { errorEventResolver.resolve(capture(event), ErrorMapperType.PAYMENT_RESUME) }

        assert(event.captured.javaClass == IllegalArgumentException::class.java)
    }

    @Test
    fun `handleNewClientToken() should dispatch resume error event when ClientTokenIntent is not HOOLAH_REDIRECTION`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { verificationTokenRepository.validate(any()) }.returns(
            flowOf(true)
        )
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)

        runTest {
            resumeHandler.continueWithNewClientToken("")
        }

        val event = slot<Throwable>()

        verify { errorEventResolver.resolve(capture(event), ErrorMapperType.PAYMENT_RESUME) }

        assert(event.captured.javaClass == IllegalArgumentException::class.java)
    }

    @Test
    fun `handleNewClientToken() should dispatch resume error event when ClientTokenIntent is PAY_NL_IDEAL_REDIRECTION and paymentMethodInstrumentType is not PAY_NL_IDEAL_IDENTIFIER`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { verificationTokenRepository.validate(any()) }.returns(
            flowOf(true)
        )
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.PAY_NL_IDEAL_REDIRECTION.name
        )
        every { paymentMethodToken.paymentInstrumentData?.paymentMethodType }.returns(
            PaymentMethodType.HOOLAH.name
        )
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)

        runTest {
            resumeHandler.continueWithNewClientToken("")
        }

        val event = slot<Throwable>()

        verify { errorEventResolver.resolve(capture(event), ErrorMapperType.PAYMENT_RESUME) }

        assert(event.captured.javaClass == IllegalArgumentException::class.java)
    }

    @Test
    fun `handleNewClientToken() should dispatch resume error event when ClientTokenIntent is HOOLAH_REDIRECTION and paymentMethodInstrumentType is not HOOLAH_IDENTIFIER`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { verificationTokenRepository.validate(any()) }.returns(
            flowOf(true)
        )
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.HOOLAH_REDIRECTION.name
        )
        every { paymentMethodToken.paymentInstrumentData?.paymentMethodType }.returns(
            PaymentMethodType.PAY_NL_IDEAL.name
        )
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)

        runTest {
            resumeHandler.continueWithNewClientToken("")
        }

        val event = slot<Throwable>()

        verify { errorEventResolver.resolve(capture(event), ErrorMapperType.PAYMENT_RESUME) }

        assert(event.captured.javaClass == IllegalArgumentException::class.java)
    }

    @Test
    fun `handleNewClientToken() should dispatch StartAsyncFlow event when ClientTokenIntent is XFERS_PAYNOW_REDIRECTION and paymentMethodType is XFERS_PAYNOW`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { verificationTokenRepository.validate(any()) }.returns(
            flowOf(true)
        )
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.XFERS_PAYNOW_REDIRECTION.name
        )
        every { paymentMethodToken.paymentInstrumentData?.paymentMethodType }.returns(
            PaymentMethodType.XFERS_PAYNOW.name
        )
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)

        runTest {
            resumeHandler.continueWithNewClientToken("")
        }

        val event = slot<List<CheckoutEvent>>()

        verify { eventDispatcher.dispatchEvents(capture(event)) }

        assertEquals(CheckoutEventType.PAYMENT_METHOD_PRESENTED, event.captured[0].type)
        assertEquals(CheckoutEventType.START_ASYNC_FLOW, event.captured[1].type)
    }

    @Test
    fun `handleNewClientToken() should dispatch resume error event when ClientTokenIntent is XFERS_PAYNOW_REDIRECTION and paymentMethodInstrumentType is not XFERS_PAYNOW_REDIRECTION`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { verificationTokenRepository.validate(any()) }.returns(
            flowOf(true)
        )
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.XFERS_PAYNOW_REDIRECTION.name
        )
        every { paymentMethodToken.paymentInstrumentData?.paymentMethodType }.returns(
            PaymentMethodType.PAY_NL_IDEAL.name
        )
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)

        runTest {
            resumeHandler.continueWithNewClientToken("")
        }

        val event = slot<Throwable>()

        verify { errorEventResolver.resolve(capture(event), ErrorMapperType.PAYMENT_RESUME) }

        assert(event.captured.javaClass == IllegalArgumentException::class.java)
    }
}
