@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.checkout.orchestrator.presentation

import android.content.Intent
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.CoroutineScopeProvider
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.tokenization.domain.handler.PreTokenizationHandler
import io.primer.checkout.orchestrator.domain.CheckoutDecisionResolver
import io.primer.checkout.orchestrator.domain.CheckoutOrchestrator
import io.primer.checkout.orchestrator.domain.ReturnUriProvider
import io.primer.checkout.orchestrator.domain.model.CheckoutDecision
import io.primer.checkout.orchestrator.domain.ui.StepUiHandler
import io.primer.executionengine.domain.models.Outcome
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import io.primer.statetransport.domain.interactor.PaymentFlowInteractor
import io.primer.statetransport.domain.model.CheckoutOutcome
import io.primer.statetransport.domain.model.ClientInstructions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.concurrent.ConcurrentHashMap

@ExtendWith(MockKExtension::class)
internal class BackendDrivenCheckoutComponentTest {
    private val scheduler = TestCoroutineScheduler()

    @JvmField
    @RegisterExtension
    val instantExecutorExtension = InstantExecutorExtension(scheduler)

    @MockK
    lateinit var orchestrator: CheckoutOrchestrator

    @MockK
    lateinit var paymentFlowInteractor: PaymentFlowInteractor

    @MockK
    lateinit var preTokenizationHandler: PreTokenizationHandler

    @MockK
    lateinit var successHandler: CheckoutSuccessHandler

    @MockK
    lateinit var errorHandler: CheckoutErrorHandler

    @MockK
    lateinit var checkoutDecisionResolver: CheckoutDecisionResolver

    @MockK
    lateinit var returnUriProvider: ReturnUriProvider

    @MockK
    lateinit var baseErrorResolver: BaseErrorResolver

    @RelaxedMockK
    lateinit var stepUiHandler: StepUiHandler

    private val paymentMethodType = "PAYMENT_CARD"
    private val primerSessionIntent = PrimerSessionIntent.CHECKOUT
    private val returnUri = "primer://return"

    private lateinit var component: BackendDrivenCheckoutComponent

    @BeforeEach
    fun setUp() {
        DISdkContext.headlessSdkContainer =
            mockk<SdkContainer>(relaxed = true).also { sdkContainer ->
                val cont =
                    spyk<DependencyContainer>().also { container ->
                        container.registerFactory<CoroutineScopeProvider> {
                            object : CoroutineScopeProvider {
                                override val scope: CoroutineScope
                                    get() = TestScope()
                            }
                        }
                    }
                every { sdkContainer.containers }
                    .returns(ConcurrentHashMap(mutableMapOf(cont::class.simpleName.orEmpty() to cont)))
            }

        coEvery { returnUriProvider.provide() } returns returnUri
        coEvery { preTokenizationHandler.handle(any(), any()) } returns Result.success(Unit)

        component = BackendDrivenCheckoutComponent(
            orchestrator = orchestrator,
            paymentFlowInteractor = paymentFlowInteractor,
            preTokenizationHandler = preTokenizationHandler,
            successHandler = successHandler,
            errorHandler = errorHandler,
            checkoutDecisionResolver = checkoutDecisionResolver,
            returnUriProvider = returnUriProvider,
            baseErrorResolver = baseErrorResolver,
            stepUiHandlers = listOf(stepUiHandler),
        )
    }

    @Test
    fun `start() should call successHandler when flow emits End with success decision`() {
        val payment = Payment(id = "pay_123", orderId = "order_123")
        val endInstruction = ClientInstructions.End(
            checkoutOutcome = CheckoutOutcome.CHECKOUT_COMPLETE,
            payment = null,
        )
        every {
            paymentFlowInteractor(any())
        } returns flowOf(endInstruction)
        every {
            checkoutDecisionResolver.resolve(endInstruction, paymentMethodType)
        } returns CheckoutDecision.Success(payment)
        coEvery { successHandler.handle(any(), any()) } just Runs

        component.start(paymentMethodType, primerSessionIntent)
        scheduler.advanceUntilIdle()

        coVerify {
            checkoutDecisionResolver.resolve(endInstruction, paymentMethodType)
            successHandler.handle(payment = payment, additionalInfo = null)
        }
    }

    @Test
    fun `start() should call errorHandler when flow emits End with failure decision`() {
        val primerError = mockk<PrimerError>()
        val payment = Payment(id = "pay_123", orderId = "order_123")
        val endInstruction = ClientInstructions.End(
            checkoutOutcome = CheckoutOutcome.CHECKOUT_FAILURE,
            payment = null,
        )
        every {
            paymentFlowInteractor(any())
        } returns flowOf(endInstruction)
        every {
            checkoutDecisionResolver.resolve(endInstruction, paymentMethodType)
        } returns CheckoutDecision.Failure(error = primerError, payment = payment)
        coEvery { errorHandler.handle(any(), any()) } just Runs

        component.start(paymentMethodType, primerSessionIntent)
        scheduler.advanceUntilIdle()

        coVerify {
            errorHandler.handle(error = primerError, payment = payment)
        }
    }

    @Test
    fun `start() should call orchestrator when flow emits Execute instruction`() {
        val payload = """{"action":"tokenize"}"""
        val executeInstruction = ClientInstructions.Execute(
            pollDelayMilliseconds = 1000L,
            payload = payload,
        )
        every {
            paymentFlowInteractor(any())
        } returns flowOf(executeInstruction)
        coEvery {
            orchestrator.start(paymentMethodType, payload)
        } returns Result.success(mockk())

        component.start(paymentMethodType, primerSessionIntent)
        scheduler.advanceUntilIdle()

        coVerify {
            orchestrator.start(
                paymentMethodType = paymentMethodType,
                payload = payload,
            )
        }
    }

    @Test
    fun `start() should call errorHandler when orchestrator fails`() {
        val payload = """{"action":"tokenize"}"""
        val executeInstruction = ClientInstructions.Execute(
            pollDelayMilliseconds = 1000L,
            payload = payload,
        )
        val throwable = RuntimeException("orchestrator failed")
        val primerError = mockk<PrimerError>()

        every {
            paymentFlowInteractor(any())
        } returns flowOf(executeInstruction)
        coEvery {
            orchestrator.start(paymentMethodType, payload)
        } returns Result.failure(throwable)
        every { baseErrorResolver.resolve(throwable) } returns primerError
        coEvery { errorHandler.handle(any(), any()) } just Runs

        component.start(paymentMethodType, primerSessionIntent)
        scheduler.advanceUntilIdle()

        coVerify {
            baseErrorResolver.resolve(throwable)
            errorHandler.handle(error = primerError, payment = null)
        }
    }

    @Test
    fun `start() should cancel polling and emit PaymentMethodCancelledException when orchestrator returns CANCELLED`() {
        val payload = """{"action":"tokenize"}"""
        val executeInstruction = ClientInstructions.Execute(
            pollDelayMilliseconds = 1000L,
            payload = payload,
        )
        val primerError = mockk<PrimerError>()

        every {
            paymentFlowInteractor(any())
        } returns flowOf(executeInstruction)
        coEvery {
            orchestrator.start(paymentMethodType, payload)
        } returns Result.success(Outcome.CANCELLED)
        every {
            baseErrorResolver.resolve(ofType(PaymentMethodCancelledException::class))
        } returns primerError
        coEvery { errorHandler.handle(any(), any()) } just Runs

        component.start(paymentMethodType, primerSessionIntent)
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            errorHandler.handle(error = primerError, payment = null)
        }
        verify {
            baseErrorResolver.resolve(
                match<PaymentMethodCancelledException> { it.paymentMethodType == paymentMethodType },
            )
        }
    }

    @Test
    fun `start() should call errorHandler when paymentFlowInteractor flow errors`() {
        val throwable = RuntimeException("flow error")
        val primerError = mockk<PrimerError>()

        every {
            paymentFlowInteractor(any())
        } returns flow { throw throwable }
        every { baseErrorResolver.resolve(throwable) } returns primerError
        coEvery { errorHandler.handle(any(), any()) } just Runs

        component.start(paymentMethodType, primerSessionIntent)
        scheduler.advanceUntilIdle()

        coVerify {
            baseErrorResolver.resolve(throwable)
            errorHandler.handle(error = primerError, payment = null)
        }
    }

    @Test
    fun `start() should do nothing when flow emits Wait instruction`() {
        val waitInstruction = ClientInstructions.Wait(pollDelayMilliseconds = 2000L)
        every {
            paymentFlowInteractor(any())
        } returns flowOf(waitInstruction)

        component.start(paymentMethodType, primerSessionIntent)
        scheduler.advanceUntilIdle()

        verify {
            paymentFlowInteractor(any())
        }
        coVerify(exactly = 0) {
            orchestrator.start(any(), any())
            successHandler.handle(any(), any())
            errorHandler.handle(any(), any())
        }
    }

    @Test
    fun `start() should observe step UI handler launch requests`() {
        every {
            paymentFlowInteractor(any())
        } returns flowOf(ClientInstructions.Wait(pollDelayMilliseconds = 1000L))

        component.start(paymentMethodType, primerSessionIntent)

        verify {
            stepUiHandler.observeLaunchRequests(any(), eq(paymentMethodType), any())
        }
    }

    @Test
    fun `handleActivityStartEvent() should emit UI event when handler returns event`() {
        val params = mockk<PaymentMethodLauncherParams>()
        val navigateEvent = mockk<ComposerUiEvent>()
        every { stepUiHandler.handleActivityStartEvent(params) } returns navigateEvent

        every {
            paymentFlowInteractor(any())
        } returns flowOf(ClientInstructions.Wait(pollDelayMilliseconds = 1000L))
        component.start(paymentMethodType, primerSessionIntent)
        scheduler.advanceUntilIdle()

        val events = mutableListOf<ComposerUiEvent>()
        val collectJob = CoroutineScope(Dispatchers.Main).launch {
            component.uiEvent.collect { events.add(it) }
        }
        scheduler.advanceUntilIdle()

        component.handleActivityStartEvent(params)
        scheduler.advanceUntilIdle()

        assertEquals(listOf(navigateEvent), events)
        collectJob.cancel()
    }

    @Test
    fun `handleActivityStartEvent() should not emit UI event when no handler handles the event`() {
        val params = mockk<PaymentMethodLauncherParams>()
        every { stepUiHandler.handleActivityStartEvent(params) } returns null

        every {
            paymentFlowInteractor(any())
        } returns flowOf(ClientInstructions.Wait(pollDelayMilliseconds = 1000L))
        component.start(paymentMethodType, primerSessionIntent)
        scheduler.advanceUntilIdle()

        val events = mutableListOf<ComposerUiEvent>()
        val collectJob = CoroutineScope(Dispatchers.Main).launch {
            component.uiEvent.collect { events.add(it) }
        }
        scheduler.advanceUntilIdle()

        component.handleActivityStartEvent(params)
        scheduler.advanceUntilIdle()

        assertEquals(emptyList<ComposerUiEvent>(), events)
        collectJob.cancel()
    }

    @Test
    fun `start() should invoke preTokenizationHandler before paymentFlowInteractor`() {
        val endInstruction = ClientInstructions.End(
            checkoutOutcome = CheckoutOutcome.CHECKOUT_COMPLETE,
            payment = null,
        )
        val payment = Payment(id = "pay_123", orderId = "order_123")
        every { paymentFlowInteractor(any()) } returns flowOf(endInstruction)
        every {
            checkoutDecisionResolver.resolve(endInstruction, paymentMethodType)
        } returns CheckoutDecision.Success(payment)
        coEvery { successHandler.handle(any(), any()) } just Runs

        component.start(paymentMethodType, primerSessionIntent)
        scheduler.advanceUntilIdle()

        coVerify(ordering = io.mockk.Ordering.ORDERED) {
            preTokenizationHandler.handle(paymentMethodType, primerSessionIntent)
            paymentFlowInteractor(any())
        }
    }

    @Test
    fun `start() should call errorHandler and skip paymentFlowInteractor when preTokenizationHandler aborts`() {
        val throwable = IllegalStateException("developer aborted")
        val primerError = mockk<PrimerError>()
        coEvery {
            preTokenizationHandler.handle(paymentMethodType, primerSessionIntent)
        } returns Result.failure(throwable)
        every { baseErrorResolver.resolve(throwable) } returns primerError
        coEvery { errorHandler.handle(any(), any()) } just Runs

        component.start(paymentMethodType, primerSessionIntent)
        scheduler.advanceUntilIdle()

        coVerify {
            baseErrorResolver.resolve(throwable)
            errorHandler.handle(error = primerError, payment = null)
        }
        coVerify(exactly = 0) { paymentFlowInteractor(any()) }
    }

    @Test
    fun `handleActivityResultIntent() should delegate to step UI handlers and emit Finish`() {
        val params = mockk<PaymentMethodLauncherParams>()
        val intent = mockk<Intent>()
        val resultCode = 1
        every { stepUiHandler.handleActivityResult(params, resultCode, intent) } returns true

        every {
            paymentFlowInteractor(any())
        } returns flowOf(ClientInstructions.Wait(pollDelayMilliseconds = 1000L))
        component.start(paymentMethodType, primerSessionIntent)
        scheduler.advanceUntilIdle()

        val events = mutableListOf<ComposerUiEvent>()
        val collectJob = CoroutineScope(Dispatchers.Main).launch {
            component.uiEvent.collect { events.add(it) }
        }
        scheduler.advanceUntilIdle()

        component.handleActivityResultIntent(params, resultCode, intent)
        scheduler.advanceUntilIdle()

        assertEquals(listOf(ComposerUiEvent.Finish), events)
        verify { stepUiHandler.handleActivityResult(params, resultCode, intent) }
        collectJob.cancel()
    }
}
