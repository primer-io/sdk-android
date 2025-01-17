package io.primer.android.paymentMethods.core.data.repository

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.Primer
import io.primer.android.PrimerCheckoutListener
import io.primer.android.PrimerSessionIntent
import io.primer.android.completion.PrimerErrorDecisionHandler
import io.primer.android.completion.PrimerHeadlessUniversalCheckoutResumeDecisionHandler
import io.primer.android.completion.PrimerPaymentCreationDecisionHandler
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.PrimerHeadlessUniversalCheckoutListener
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.settings.internal.PrimerIntent
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodData
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.errors.domain.models.PaymentMethodCancelledError
import io.primer.android.paymentMethods.core.domain.events.PrimerEvent
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.ui.fragments.ErrorType
import io.primer.android.ui.fragments.SuccessType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.seconds

class DefaultPrimerHeadlessRepositoryTest {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var config: PrimerConfig

    @MockK
    private lateinit var headlessUniversalCheckout: PrimerHeadlessUniversalCheckout

    @MockK
    private lateinit var primerCheckoutListener: PrimerCheckoutListener

    private lateinit var repository: DefaultPrimerHeadlessRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(PrimerHeadlessUniversalCheckout, Primer.current)
        every { PrimerHeadlessUniversalCheckout.current } returns headlessUniversalCheckout
        every { Primer.current.listener } returns primerCheckoutListener
        repository = DefaultPrimerHeadlessRepository(context = context, config = config)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(PrimerHeadlessUniversalCheckout)
    }

    @Test
    fun `start() should call headlessUniversalCheckout start() with context and clientToken`() {
        val clientToken = "clientToken"
        every { headlessUniversalCheckout.start(any(), any()) } just Runs

        repository.start(clientToken)

        verify { headlessUniversalCheckout.start(context, clientToken) }
    }

    @Test
    fun `handleManualFlowSuccess() should emit CheckoutCompleted event when isSuccessScreenEnabled=true`() =
        runTest {
            val checkoutAdditionalInfo = mockk<PrimerCheckoutAdditionalInfo>()
            val intent =
                mockk<PrimerIntent> {
                    every { paymentMethodIntent } returns PrimerSessionIntent.CHECKOUT
                }
            every { config.intent } returns intent
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns true
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } just Runs

            repository.handleManualFlowSuccess(checkoutAdditionalInfo)
            val event = repository.events.first()

            assert(event is PrimerEvent.CheckoutCompleted)
            assertEquals(
                PrimerCheckoutData(Payment.undefined, checkoutAdditionalInfo),
                (event as PrimerEvent.CheckoutCompleted).checkoutData,
            )
            assertEquals(SuccessType.PAYMENT_SUCCESS, event.successType)
            verify {
                config.settings
                config.intent
            }
            verify(exactly = 0) {
                primerCheckoutListener.onCheckoutCompleted(any())
            }
        }

    @Test
    fun `handleManualFlowSuccess() should emit CheckoutCompleted event when isSuccessScreenEnabled=false`() =
        runTest {
            val intent =
                mockk<PrimerIntent> {
                    every { paymentMethodIntent } returns PrimerSessionIntent.CHECKOUT
                }
            every { config.intent } returns intent
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns false
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } just Runs

            repository.handleManualFlowSuccess(mockk())
            val event = repository.events.first()

            assertIs<PrimerEvent.Dismiss>(event)
            verify {
                config.settings
            }
            verify(exactly = 0) {
                config.intent
                primerCheckoutListener.onCheckoutCompleted(any())
            }
        }

    @Test
    fun `events flow should emit AvailablePaymentMethodsLoaded event when onAvailablePaymentMethodsLoaded() is called`() =
        runTest {
            val paymentMethods = listOf<PrimerHeadlessUniversalCheckoutPaymentMethod>(mockk())
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onAvailablePaymentMethodsLoaded(paymentMethods)
            }

            val event = repository.events.first()

            assert(event is PrimerEvent.AvailablePaymentMethodsLoaded)
            assertEquals(
                paymentMethods,
                (event as PrimerEvent.AvailablePaymentMethodsLoaded).paymentMethodsHolder.paymentMethods,
            )
        }

    @Test
    fun `events flow should call PrimerCheckoutListener and emit CheckoutCompleted event when onCheckoutCompleted() is called and isSuccessScreenEnabled=true`() =
        runTest {
            val checkoutData = mockk<PrimerCheckoutData>()
            val intent =
                mockk<PrimerIntent> {
                    every { paymentMethodIntent } returns PrimerSessionIntent.CHECKOUT
                }
            every { config.intent } returns intent
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns true
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onCheckoutCompleted(checkoutData)
            }
            every { primerCheckoutListener.onCheckoutCompleted(any()) } just Runs

            val event = repository.events.first()

            assert(event is PrimerEvent.CheckoutCompleted)
            assertEquals(checkoutData, (event as PrimerEvent.CheckoutCompleted).checkoutData)
            assertEquals(SuccessType.PAYMENT_SUCCESS, event.successType)
            verify {
                config.settings
                config.intent
                primerCheckoutListener.onCheckoutCompleted(checkoutData)
            }
        }

    @Test
    fun `events flow should call PrimerCheckoutListener and emit Dismiss event when onCheckoutCompleted() is called and isSuccessScreenEnabled=false`() =
        runTest {
            val checkoutData = mockk<PrimerCheckoutData>()
            val intent =
                mockk<PrimerIntent> {
                    every { paymentMethodIntent } returns PrimerSessionIntent.CHECKOUT
                }
            every { config.intent } returns intent
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns false
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onCheckoutCompleted(checkoutData)
            }
            every { primerCheckoutListener.onCheckoutCompleted(any()) } just Runs

            val event = repository.events.first()

            assertIs<PrimerEvent.Dismiss>(event)
            verify {
                config.settings
                primerCheckoutListener.onCheckoutCompleted(checkoutData)
            }
            verify(exactly = 0) {
                config.intent
            }
        }

    @Test
    fun `events flow should call PrimerCheckoutListener and emit CheckoutFailed event with cancellation error when onFailed(2) is called and isSuccessScreenEnabled=true and PrimerSessionIntent CHECKOUT`() =
        runTest {
            val error = mockk<PaymentMethodCancelledError>()
            val intent =
                mockk<PrimerIntent> {
                    every { paymentMethodIntent } returns PrimerSessionIntent.CHECKOUT
                }
            every { config.intent } returns intent
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns true
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            val checkoutData = mockk<PrimerCheckoutData>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onFailed(error, checkoutData)
            }
            val handlerSlot = slot<PrimerErrorDecisionHandler>()
            every { primerCheckoutListener.onFailed(any(), any(), capture(handlerSlot)) } answers {
                handlerSlot.captured.showErrorMessage("This is an error")
            }

            val event = repository.events.first()

            assert(event is PrimerEvent.CheckoutFailed)
            assertEquals("This is an error", (event as PrimerEvent.CheckoutFailed).errorMessage)
            assertEquals(ErrorType.PAYMENT_CANCELLED, event.errorType)
            verify {
                intent.paymentMethodIntent
                primerCheckoutListener.onFailed(error, checkoutData, handlerSlot.captured)
            }
        }

    @Test
    fun `events flow should call PrimerCheckoutListener and emit CheckoutFailed event with error when onFailed(2) is called and isSuccessScreenEnabled=true and PrimerSessionIntent CHECKOUT`() =
        runTest {
            val error = mockk<PrimerError>()
            val intent =
                mockk<PrimerIntent> {
                    every { paymentMethodIntent } returns PrimerSessionIntent.CHECKOUT
                }
            every { config.intent } returns intent
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns true
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            val checkoutData = mockk<PrimerCheckoutData>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onFailed(error, checkoutData)
            }
            val handlerSlot = slot<PrimerErrorDecisionHandler>()
            every { primerCheckoutListener.onFailed(any(), any(), capture(handlerSlot)) } answers {
                handlerSlot.captured.showErrorMessage("This is an error")
            }

            val event = repository.events.first()

            assert(event is PrimerEvent.CheckoutFailed)
            assertEquals("This is an error", (event as PrimerEvent.CheckoutFailed).errorMessage)
            assertEquals(ErrorType.PAYMENT_FAILED, event.errorType)
            verify {
                intent.paymentMethodIntent
                primerCheckoutListener.onFailed(error, checkoutData, handlerSlot.captured)
            }
        }

    @Test
    fun `events flow should call PrimerCheckoutListener and emit CheckoutFailed event with error when onFailed(2) is called and isSuccessScreenEnabled=true and PrimerSessionIntent VAULT`() =
        runTest {
            val error = mockk<PrimerError>()
            val intent =
                mockk<PrimerIntent> {
                    every { paymentMethodIntent } returns PrimerSessionIntent.VAULT
                }
            every { config.intent } returns intent
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns true
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            val checkoutData = mockk<PrimerCheckoutData>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onFailed(error, checkoutData)
            }
            val handlerSlot = slot<PrimerErrorDecisionHandler>()
            every { primerCheckoutListener.onFailed(any(), any(), capture(handlerSlot)) } answers {
                handlerSlot.captured.showErrorMessage("This is an error")
            }

            val event = repository.events.first()

            assert(event is PrimerEvent.CheckoutFailed)
            assertEquals("This is an error", (event as PrimerEvent.CheckoutFailed).errorMessage)
            assertEquals(ErrorType.VAULT_TOKENIZATION_FAILED, event.errorType)
            verify {
                intent.paymentMethodIntent
                primerCheckoutListener.onFailed(error, checkoutData, handlerSlot.captured)
            }
        }

    @Test
    fun `events flow should call PrimerCheckoutListener and emit Dismiss event when onFailed(2) is called and isSuccessScreenEnabled=false`() =
        runTest {
            val error = mockk<PrimerError>()
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns false
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            val checkoutData = mockk<PrimerCheckoutData>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onFailed(error, checkoutData)
            }
            val handlerSlot = slot<PrimerErrorDecisionHandler>()
            every { primerCheckoutListener.onFailed(any(), any(), capture(handlerSlot)) } answers {
                handlerSlot.captured.showErrorMessage("This is an error")
            }

            val event = repository.events.first()

            assertIs<PrimerEvent.Dismiss>(event)
            verify {
                primerCheckoutListener.onFailed(error, checkoutData, handlerSlot.captured)
            }
            verify(exactly = 0) {
                config.intent.paymentMethodIntent
            }
        }

    @Test
    fun `events flow should call PrimerCheckoutListener and emit CheckoutFailed event with cancellation error when onFailed(1) is called and isSuccessScreenEnabled=true and PrimerSessionIntent CHECKOUT`() =
        runTest {
            val error = mockk<PaymentMethodCancelledError>()
            val intent =
                mockk<PrimerIntent> {
                    every { paymentMethodIntent } returns PrimerSessionIntent.CHECKOUT
                }
            every { config.intent } returns intent
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns true
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onFailed(error)
            }
            val handlerSlot = slot<PrimerErrorDecisionHandler>()
            every { primerCheckoutListener.onFailed(any(), capture(handlerSlot)) } answers {
                handlerSlot.captured.showErrorMessage("This is an error")
            }

            val event = repository.events.first()

            assert(event is PrimerEvent.CheckoutFailed)
            assertEquals("This is an error", (event as PrimerEvent.CheckoutFailed).errorMessage)
            assertEquals(ErrorType.PAYMENT_CANCELLED, event.errorType)
            verify {
                intent.paymentMethodIntent
                primerCheckoutListener.onFailed(error, handlerSlot.captured)
            }
        }

    @Test
    fun `events flow should call PrimerCheckoutListener and emit CheckoutFailed event with error when onFailed(1) is called and isSuccessScreenEnabled=true and PrimerSessionIntent CHECKOUT`() =
        runTest {
            val error = mockk<PrimerError>()
            val intent =
                mockk<PrimerIntent> {
                    every { paymentMethodIntent } returns PrimerSessionIntent.CHECKOUT
                }
            every { config.intent } returns intent
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns true
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onFailed(error)
            }
            val handlerSlot = slot<PrimerErrorDecisionHandler>()
            every { primerCheckoutListener.onFailed(any(), capture(handlerSlot)) } answers {
                handlerSlot.captured.showErrorMessage("This is an error")
            }

            val event = repository.events.first()

            assert(event is PrimerEvent.CheckoutFailed)
            assertEquals("This is an error", (event as PrimerEvent.CheckoutFailed).errorMessage)
            assertEquals(ErrorType.PAYMENT_FAILED, event.errorType)
            verify {
                intent.paymentMethodIntent
                primerCheckoutListener.onFailed(error, handlerSlot.captured)
            }
        }

    @Test
    fun `events flow should call PrimerCheckoutListener and emit CheckoutFailed event with error when onFailed(1) is called and isSuccessScreenEnabled=true and PrimerSessionIntent VAULT`() =
        runTest {
            val error = mockk<PrimerError>()
            val intent =
                mockk<PrimerIntent> {
                    every { paymentMethodIntent } returns PrimerSessionIntent.VAULT
                }
            every { config.intent } returns intent
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns true
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onFailed(error)
            }
            val handlerSlot = slot<PrimerErrorDecisionHandler>()
            every { primerCheckoutListener.onFailed(any(), capture(handlerSlot)) } answers {
                handlerSlot.captured.showErrorMessage("This is an error")
            }

            val event = repository.events.first()

            assert(event is PrimerEvent.CheckoutFailed)
            assertEquals("This is an error", (event as PrimerEvent.CheckoutFailed).errorMessage)
            assertEquals(ErrorType.VAULT_TOKENIZATION_FAILED, event.errorType)
            verify {
                intent.paymentMethodIntent
                primerCheckoutListener.onFailed(error, handlerSlot.captured)
            }
        }

    @Test
    fun `events flow should call PrimerCheckoutListener and emit Dismiss event when onFailed(1) is called and isSuccessScreenEnabled=false`() =
        runTest {
            val error = mockk<PrimerError>()
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns false
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onFailed(error)
            }
            val handlerSlot = slot<PrimerErrorDecisionHandler>()
            every { primerCheckoutListener.onFailed(any(), capture(handlerSlot)) } answers {
                handlerSlot.captured.showErrorMessage("This is an error")
            }

            val event = repository.events.first()

            assertIs<PrimerEvent.Dismiss>(event)
            verify {
                primerCheckoutListener.onFailed(error, handlerSlot.captured)
            }
            verify(exactly = 0) {
                config.intent.paymentMethodIntent
            }
        }

    @Test
    fun `events flow should call PrimerCheckoutListener when onTokenizeSuccess() is called and delegates to decision handler when continueWithNewClientToken() is called`() =
        runTest {
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>()
            val decisionHandler =
                mockk<PrimerHeadlessUniversalCheckoutResumeDecisionHandler> {
                    every { continueWithNewClientToken(any()) } just Runs
                }
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onTokenizeSuccess(paymentMethodTokenData, decisionHandler)
            }
            every { primerCheckoutListener.onTokenizeSuccess(any(), any()) } just Runs

            runCatching {
                withTimeout(1.seconds) {
                    repository.events.collect {}
                }
            }

            val slot = slot<PrimerResumeDecisionHandler>()
            verify {
                primerCheckoutListener.onTokenizeSuccess(paymentMethodTokenData, capture(slot))
            }
            slot.captured.continueWithNewClientToken("token")
            verify {
                decisionHandler.continueWithNewClientToken("token")
            }
        }

    @Test
    fun `events flow should call PrimerCheckoutListener when onTokenizeSuccess() is called and success should be handled correctly when isSuccessScreenEnabled=false`() =
        runTest {
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            val intent =
                mockk<PrimerIntent> {
                    every { paymentMethodIntent } returns PrimerSessionIntent.CHECKOUT
                }
            every { config.intent } returns intent
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns false
            val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>()
            val decisionHandler = mockk<PrimerHeadlessUniversalCheckoutResumeDecisionHandler>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onTokenizeSuccess(paymentMethodTokenData, decisionHandler)
            }
            every { primerCheckoutListener.onTokenizeSuccess(any(), any()) } just Runs

            val events = mutableListOf<PrimerEvent>()
            val collectJob =
                launch {
                    repository.events.collectLatest {
                        events += it
                    }
                }
            delay(1.seconds)

            val slot = slot<PrimerResumeDecisionHandler>()
            verify {
                primerCheckoutListener.onTokenizeSuccess(paymentMethodTokenData, capture(slot))
            }
            slot.captured.handleSuccess()
            verify(exactly = 0) {
                decisionHandler.continueWithNewClientToken(any())
            }
            delay(1.seconds)
            assertIs<PrimerEvent.Dismiss>(events.single())
            verify {
                config.settings
            }
            verify(exactly = 0) {
                config.intent
            }
            collectJob.cancel()
        }

    @Test
    fun `events flow should call PrimerCheckoutListener when onTokenizeSuccess() is called and success should be handled correctly when isSuccessScreenEnabled=true`() =
        runTest {
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            val intent =
                mockk<PrimerIntent> {
                    every { paymentMethodIntent } returns PrimerSessionIntent.CHECKOUT
                }
            every { config.intent } returns intent
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns true
            val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>()
            val decisionHandler = mockk<PrimerHeadlessUniversalCheckoutResumeDecisionHandler>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onTokenizeSuccess(paymentMethodTokenData, decisionHandler)
            }
            every { primerCheckoutListener.onTokenizeSuccess(any(), any()) } just Runs

            val events = mutableListOf<PrimerEvent>()
            val collectJob =
                launch {
                    repository.events.collectLatest {
                        events += it
                    }
                }
            delay(1.seconds)

            val slot = slot<PrimerResumeDecisionHandler>()
            verify {
                primerCheckoutListener.onTokenizeSuccess(paymentMethodTokenData, capture(slot))
            }
            slot.captured.handleSuccess()
            verify(exactly = 0) {
                decisionHandler.continueWithNewClientToken(any())
            }
            delay(1.seconds)
            val event = events.single()
            assert(event is PrimerEvent.CheckoutCompleted)
            assertEquals(null, (event as PrimerEvent.CheckoutCompleted).checkoutData)
            assertEquals(SuccessType.PAYMENT_SUCCESS, event.successType)
            verify {
                config.settings
                config.intent
            }
            collectJob.cancel()
        }

    @Test
    fun `events flow should call PrimerCheckoutListener when onTokenizeSuccess() is called and failure should be handled correctly when isSuccessScreenEnabled=true`() =
        runTest {
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            val intent =
                mockk<PrimerIntent> {
                    every { paymentMethodIntent } returns PrimerSessionIntent.CHECKOUT
                }
            every { config.intent } returns intent
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns true
            val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>()
            val decisionHandler = mockk<PrimerHeadlessUniversalCheckoutResumeDecisionHandler>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onTokenizeSuccess(paymentMethodTokenData, decisionHandler)
            }
            every { primerCheckoutListener.onTokenizeSuccess(any(), any()) } just Runs

            val events = mutableListOf<PrimerEvent>()
            val collectJob =
                launch {
                    repository.events.collectLatest {
                        events += it
                    }
                }
            delay(1.seconds)

            val slot = slot<PrimerResumeDecisionHandler>()
            verify {
                primerCheckoutListener.onTokenizeSuccess(paymentMethodTokenData, capture(slot))
            }
            slot.captured.handleFailure("Failure")
            verify(exactly = 0) {
                decisionHandler.continueWithNewClientToken(any())
            }
            delay(1.seconds)
            val event = events.single()
            assert(event is PrimerEvent.CheckoutFailed)
            assertEquals("Failure", (event as PrimerEvent.CheckoutFailed).errorMessage)
            assertEquals(ErrorType.PAYMENT_FAILED, event.errorType)
            verify {
                config.settings
                config.intent
            }
            collectJob.cancel()
        }

    @Test
    fun `events flow should call PrimerCheckoutListener when onTokenizeSuccess() is called and failure should be handled correctly when isSuccessScreenEnabled=false`() =
        runTest {
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            val intent =
                mockk<PrimerIntent> {
                    every { paymentMethodIntent } returns PrimerSessionIntent.CHECKOUT
                }
            every { config.intent } returns intent
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns false
            val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>()
            val decisionHandler = mockk<PrimerHeadlessUniversalCheckoutResumeDecisionHandler>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onTokenizeSuccess(paymentMethodTokenData, decisionHandler)
            }
            every { primerCheckoutListener.onTokenizeSuccess(any(), any()) } just Runs

            val events = mutableListOf<PrimerEvent>()
            val collectJob =
                launch {
                    repository.events.collectLatest {
                        events += it
                    }
                }
            delay(1.seconds)

            val slot = slot<PrimerResumeDecisionHandler>()
            verify {
                primerCheckoutListener.onTokenizeSuccess(paymentMethodTokenData, capture(slot))
            }
            slot.captured.handleFailure("Failure")
            verify(exactly = 0) {
                decisionHandler.continueWithNewClientToken(any())
            }
            delay(1.seconds)
            assertIs<PrimerEvent.Dismiss>(events.single())
            verify {
                config.settings
            }
            verify(exactly = 0) {
                config.intent
                collectJob.cancel()
            }
        }

    @Test
    fun `events flow should call PrimerCheckoutListener when onCheckoutResume() is called`() =
        runTest {
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            val resumeToken = "resumeToken"
            val decisionHandler =
                mockk<PrimerHeadlessUniversalCheckoutResumeDecisionHandler> {
                    every { continueWithNewClientToken(any()) } just Runs
                }
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onCheckoutResume(resumeToken, decisionHandler)
            }
            every { primerCheckoutListener.onResumeSuccess(any(), any()) } just Runs

            runCatching {
                withTimeout(1.seconds) {
                    repository.events.collect {}
                }
            }

            val slot = slot<PrimerResumeDecisionHandler>()
            verify {
                primerCheckoutListener.onResumeSuccess(resumeToken, capture(slot))
            }
            slot.captured.continueWithNewClientToken("token")
            verify {
                decisionHandler.continueWithNewClientToken("token")
            }
        }

    @Test
    fun `events flow should call PrimerCheckoutListener when onCheckoutResume() is called and success should be handled correctly when isSuccessScreenEnabled=false`() =
        runTest {
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            val intent =
                mockk<PrimerIntent> {
                    every { paymentMethodIntent } returns PrimerSessionIntent.CHECKOUT
                }
            every { config.intent } returns intent
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns false
            val resumeToken = "resumeToken"
            val decisionHandler = mockk<PrimerHeadlessUniversalCheckoutResumeDecisionHandler>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onCheckoutResume(resumeToken, decisionHandler)
            }
            every { primerCheckoutListener.onResumeSuccess(any(), any()) } just Runs

            val events = mutableListOf<PrimerEvent>()
            val collectJob =
                launch {
                    repository.events.collectLatest {
                        events += it
                    }
                }
            delay(1.seconds)

            val slot = slot<PrimerResumeDecisionHandler>()
            verify {
                primerCheckoutListener.onResumeSuccess(resumeToken, capture(slot))
            }
            slot.captured.handleSuccess()
            verify(exactly = 0) {
                decisionHandler.continueWithNewClientToken(any())
            }
            delay(1.seconds)
            assertIs<PrimerEvent.Dismiss>(events.single())
            verify {
                config.settings
            }
            verify(exactly = 0) {
                config.intent
            }
            collectJob.cancel()
        }

    @Test
    fun `events flow should call PrimerCheckoutListener when onCheckoutResume() is called and success should be handled correctly when isSuccessScreenEnabled=true`() =
        runTest {
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            val intent =
                mockk<PrimerIntent> {
                    every { paymentMethodIntent } returns PrimerSessionIntent.CHECKOUT
                }
            every { config.intent } returns intent
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns true
            val resumeToken = "resumeToken"
            val decisionHandler = mockk<PrimerHeadlessUniversalCheckoutResumeDecisionHandler>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onCheckoutResume(resumeToken, decisionHandler)
            }
            every { primerCheckoutListener.onResumeSuccess(any(), any()) } just Runs

            val events = mutableListOf<PrimerEvent>()
            val collectJob =
                launch {
                    repository.events.collectLatest {
                        events += it
                    }
                }
            delay(1.seconds)

            val slot = slot<PrimerResumeDecisionHandler>()
            verify {
                primerCheckoutListener.onResumeSuccess(resumeToken, capture(slot))
            }
            slot.captured.handleSuccess()
            verify(exactly = 0) {
                decisionHandler.continueWithNewClientToken(any())
            }
            delay(1.seconds)
            val event = events.single()
            assert(event is PrimerEvent.CheckoutCompleted)
            assertEquals(null, (event as PrimerEvent.CheckoutCompleted).checkoutData)
            assertEquals(SuccessType.PAYMENT_SUCCESS, event.successType)
            verify {
                config.settings
                config.intent
            }
            collectJob.cancel()
        }

    @Test
    fun `events flow should call PrimerCheckoutListener when onCheckoutResume() is called and failure should be handled correctly when isSuccessScreenEnabled=true`() =
        runTest {
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            val intent =
                mockk<PrimerIntent> {
                    every { paymentMethodIntent } returns PrimerSessionIntent.CHECKOUT
                }
            every { config.intent } returns intent
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns true
            val resumeToken = "resumeToken"
            val decisionHandler = mockk<PrimerHeadlessUniversalCheckoutResumeDecisionHandler>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onCheckoutResume(resumeToken, decisionHandler)
            }
            every { primerCheckoutListener.onResumeSuccess(any(), any()) } just Runs

            val events = mutableListOf<PrimerEvent>()
            val collectJob =
                launch {
                    repository.events.collectLatest {
                        events += it
                    }
                }
            delay(1.seconds)

            val slot = slot<PrimerResumeDecisionHandler>()
            verify {
                primerCheckoutListener.onResumeSuccess(resumeToken, capture(slot))
            }
            slot.captured.handleFailure("Failure")
            verify(exactly = 0) {
                decisionHandler.continueWithNewClientToken(any())
            }
            delay(1.seconds)
            val event = events.single()
            assert(event is PrimerEvent.CheckoutFailed)
            assertEquals("Failure", (event as PrimerEvent.CheckoutFailed).errorMessage)
            assertEquals(ErrorType.PAYMENT_FAILED, event.errorType)
            verify {
                config.settings
                config.intent
            }
            collectJob.cancel()
        }

    @Test
    fun `events flow should call PrimerCheckoutListener when onCheckoutResume() is called and failure should be handled correctly when isSuccessScreenEnabled=false`() =
        runTest {
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            val intent =
                mockk<PrimerIntent> {
                    every { paymentMethodIntent } returns PrimerSessionIntent.CHECKOUT
                }
            every { config.intent } returns intent
            every { config.settings.uiOptions.isSuccessScreenEnabled } returns false
            val resumeToken = "resumeToken"
            val decisionHandler = mockk<PrimerHeadlessUniversalCheckoutResumeDecisionHandler>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onCheckoutResume(resumeToken, decisionHandler)
            }
            every { primerCheckoutListener.onResumeSuccess(any(), any()) } just Runs

            val events = mutableListOf<PrimerEvent>()
            val collectJob =
                launch {
                    repository.events.collectLatest {
                        events += it
                    }
                }
            delay(1.seconds)

            val slot = slot<PrimerResumeDecisionHandler>()
            verify {
                primerCheckoutListener.onResumeSuccess(resumeToken, capture(slot))
            }
            slot.captured.handleFailure("Failure")
            verify(exactly = 0) {
                decisionHandler.continueWithNewClientToken(any())
            }
            delay(1.seconds)
            assertIs<PrimerEvent.Dismiss>(events.single())
            verify {
                config.settings
            }
            verify(exactly = 0) {
                config.intent
                collectJob.cancel()
            }
        }

    @Test
    fun `events flow should call PrimerCheckoutListener when onBeforePaymentCreated() is called`() =
        runTest {
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            val paymentMethodData = mockk<PrimerPaymentMethodData>()
            val decisionHandler = mockk<PrimerPaymentCreationDecisionHandler>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onBeforePaymentCreated(paymentMethodData, decisionHandler)
            }
            every { primerCheckoutListener.onBeforePaymentCreated(any(), any()) } just Runs

            runCatching {
                withTimeout(1.seconds) {
                    repository.events.collect {}
                }
            }

            verify {
                primerCheckoutListener.onBeforePaymentCreated(paymentMethodData, decisionHandler)
            }
        }

    @Test
    fun `events flow should call PrimerCheckoutListener when onBeforeClientSessionUpdated() is called`() =
        runTest {
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onBeforeClientSessionUpdated()
            }
            every { primerCheckoutListener.onBeforeClientSessionUpdated() } just Runs

            runCatching {
                withTimeout(1.seconds) {
                    repository.events.collect {}
                }
            }

            verify {
                primerCheckoutListener.onBeforeClientSessionUpdated()
            }
        }

    @Test
    fun `events flow should call PrimerCheckoutListener when onClientSessionUpdated() is called`() =
        runTest {
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            val clientSession = mockk<PrimerClientSession>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onClientSessionUpdated(clientSession)
            }
            every { primerCheckoutListener.onClientSessionUpdated(any()) } just Runs

            runCatching {
                withTimeout(1.seconds) {
                    repository.events.collect {}
                }
            }

            verify {
                primerCheckoutListener.onClientSessionUpdated(clientSession)
            }
        }

    @Test
    fun `events flow should call PrimerCheckoutListener when onResumePending() is called`() =
        runTest {
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            val additionalInfo = mockk<PrimerCheckoutAdditionalInfo>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onResumePending(additionalInfo)
            }
            every { primerCheckoutListener.onResumePending(any()) } just Runs

            runCatching {
                withTimeout(1.seconds) {
                    repository.events.collect {}
                }
            }

            verify {
                primerCheckoutListener.onResumePending(additionalInfo)
            }
        }

    @Test
    fun `events flow should call PrimerCheckoutListener when onCheckoutAdditionalInfoReceived() is called`() =
        runTest {
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
            val additionalInfo = mockk<PrimerCheckoutAdditionalInfo>()
            every { headlessUniversalCheckout.setCheckoutListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onCheckoutAdditionalInfoReceived(additionalInfo)
            }
            every { primerCheckoutListener.onAdditionalInfoReceived(any()) } just Runs

            runCatching {
                withTimeout(1.seconds) {
                    repository.events.collect {}
                }
            }

            verify {
                primerCheckoutListener.onAdditionalInfoReceived(additionalInfo)
            }
        }

    @Test
    fun `calling cleanup should cleanup PrimerHeadlessUniversalCheckout`() =
        runTest {
            every { headlessUniversalCheckout.cleanup() } just Runs

            repository.cleanup()

            verify {
                headlessUniversalCheckout.cleanup()
            }
        }
}
