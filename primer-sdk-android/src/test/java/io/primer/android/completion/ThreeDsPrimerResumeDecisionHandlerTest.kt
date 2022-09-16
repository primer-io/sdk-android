package io.primer.android.completion

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import io.primer.android.InstantExecutorExtension
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.exception.InvalidClientTokenException
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.domain.error.CheckoutErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.exception.ThreeDsLibraryNotFoundException
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.domain.payments.methods.repository.PaymentMethodsRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.domain.token.repository.ValidateTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.CheckoutEventType
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
class ThreeDsPrimerResumeDecisionHandlerTest {

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
    internal lateinit var threeDsSdkClassValidator: ThreeDsSdkClassValidator

    @RelaxedMockK
    internal lateinit var analyticsRepository: AnalyticsRepository

    @RelaxedMockK
    internal lateinit var verificationTokenRepository: ValidateTokenRepository

    @RelaxedMockK
    internal lateinit var errorEventResolver: CheckoutErrorEventResolver

    @RelaxedMockK
    internal lateinit var eventDispatcher: EventDispatcher

    @RelaxedMockK
    internal lateinit var config: PrimerConfig

    @RelaxedMockK
    internal lateinit var logger: Logger

    private lateinit var resumeHandler: ThreeDsPrimerResumeDecisionHandler

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        resumeHandler =
            ThreeDsPrimerResumeDecisionHandler(
                verificationTokenRepository,
                clientTokenRepository,
                paymentMethodRepository,
                paymentResultRepository,
                analyticsRepository,
                threeDsSdkClassValidator,
                errorEventResolver,
                eventDispatcher,
                logger,
                config,
                paymentMethodsRepository,
                instantExecutorExtension.dispatcher
            )
    }

    @Test
    fun `handleNewClientToken() should dispatch Start3DS event when ClientTokenIntent is 3DS_AUTHENTICATION and threeDsSdkClassValidator is3dsSdkIncluded is true`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { clientTokenRepository.getClientTokenIntent() }.returns(ClientTokenIntent.`3DS_AUTHENTICATION`.name)
        every { paymentMethodToken.paymentInstrumentType }.returns(PAYMENT_CARD_IDENTIFIER)
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)
        every { threeDsSdkClassValidator.is3dsSdkIncluded() }.returns(true)

        every { verificationTokenRepository.validate(any()) }.returns(
            flowOf(true)
        )

        runTest {
            resumeHandler.continueWithNewClientToken("")
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

        every { verificationTokenRepository.validate(any()) }.returns(
            flowOf(true)
        )

        runTest {
            resumeHandler.continueWithNewClientToken("")
        }

        val event = slot<Throwable>()

        verify { errorEventResolver.resolve(capture(event), ErrorMapperType.PAYMENT_RESUME) }

        assert(event.captured.javaClass == IllegalArgumentException::class.java)
    }

    @Test
    fun `handleNewClientToken() should dispatch resume error event when ClientTokenIntent is 3DS_AUTHENTICATION and paymentMethodInstrumentType is PAYMENT_CARD and threeDsSdkClassValidator is3dsSdkIncluded is false`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { clientTokenRepository.getClientTokenIntent() }.returns(ClientTokenIntent.`3DS_AUTHENTICATION`.name)
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)
        every { paymentMethodToken.paymentInstrumentType }.returns(PAYMENT_CARD_IDENTIFIER)
        every { threeDsSdkClassValidator.is3dsSdkIncluded() }.returns(false)
        every { verificationTokenRepository.validate(any()) }.returns(
            flowOf(true)
        )

        runTest {
            resumeHandler.continueWithNewClientToken("")
        }

        val event1 = slot<Throwable>()
        val event2 = slot<Throwable>()

        verifyOrder {
            errorEventResolver.resolve(capture(event1), ErrorMapperType.PAYMENT_RESUME)
            errorEventResolver.resolve(capture(event2), ErrorMapperType.PAYMENT_RESUME)
        }

        assert(event1.captured.javaClass == ThreeDsLibraryNotFoundException::class.java)
        assert(event2.captured.javaClass == InvalidClientTokenException::class.java)
    }

    @Test
    fun `handleNewClientToken() should dispatch resume error event when ClientTokenIntent is 3DS_AUTHENTICATION and paymentMethodInstrumentType is not PAYMENT_CARD`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>(relaxed = true)
        every { clientTokenRepository.getClientTokenIntent() }.returns(ClientTokenIntent.`3DS_AUTHENTICATION`.name)
        every { paymentMethodRepository.getPaymentMethod() }.returns(paymentMethodToken)
        every { verificationTokenRepository.validate(any()) }.returns(
            flowOf(true)
        )

        runTest {
            resumeHandler.continueWithNewClientToken("")
        }

        val event = slot<Throwable>()

        verify { errorEventResolver.resolve(capture(event), ErrorMapperType.PAYMENT_RESUME) }

        assert(event.captured.javaClass == IllegalArgumentException::class.java)
    }

    private companion object {
        val PAYMENT_CARD_IDENTIFIER = PaymentMethodType.PAYMENT_CARD.name
    }
}
