package io.primer.checkout.orchestrator.presentation

import android.content.Intent
import io.primer.android.PrimerSessionIntent
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.paymentmethods.core.composer.InternalNativeUiPaymentMethodComponent
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
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityResultIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityStartIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import io.primer.statetransport.domain.interactor.PaymentFlowInteractor
import io.primer.statetransport.domain.model.ClientInstructions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class BackendDrivenCheckoutComponent(
    private val orchestrator: CheckoutOrchestrator,
    private val paymentFlowInteractor: PaymentFlowInteractor,
    private val preTokenizationHandler: PreTokenizationHandler,
    private val successHandler: CheckoutSuccessHandler,
    private val errorHandler: CheckoutErrorHandler,
    private val checkoutDecisionResolver: CheckoutDecisionResolver,
    private val returnUriProvider: ReturnUriProvider,
    private val baseErrorResolver: BaseErrorResolver,
    private val stepUiHandlers: List<StepUiHandler>,
) : InternalNativeUiPaymentMethodComponent(),
    ActivityStartIntentHandler,
    ActivityResultIntentHandler {

    override val _uiEvent: MutableSharedFlow<ComposerUiEvent> = MutableSharedFlow()
    override val uiEvent: SharedFlow<ComposerUiEvent> = _uiEvent

    override fun start(
        paymentMethodType: String,
        primerSessionIntent: PrimerSessionIntent,
    ) {
        this.paymentMethodType = paymentMethodType
        this.primerSessionIntent = primerSessionIntent

        stepUiHandlers.forEach { handler ->
            handler.observeLaunchRequests(composerScope, paymentMethodType) { event ->
                _uiEvent.emit(event)
            }
        }

        composerScope.launch {
            val preTokenizationResult = preTokenizationHandler.handle(
                paymentMethodType = paymentMethodType,
                sessionIntent = primerSessionIntent,
            )
            preTokenizationResult.exceptionOrNull()?.let { throwable ->
                errorHandler.handle(
                    error = baseErrorResolver.resolve(throwable),
                    payment = null,
                )
                return@launch
            }

            paymentFlowInteractor(
                PaymentFlowInteractor.PaymentFlowParams(
                    paymentMethodType = paymentMethodType,
                    returnUri = returnUriProvider.provide(),
                ),
            ).onEach { instructions ->
                when (instructions) {
                    is ClientInstructions.Execute -> {
                        val outcome = orchestrator.start(
                            paymentMethodType = paymentMethodType,
                            payload = instructions.payload,
                        ).getOrThrow()
                        if (outcome == Outcome.CANCELLED) {
                            throw PaymentMethodCancelledException(paymentMethodType)
                        }
                    }

                    is ClientInstructions.End -> {
                        val decision = checkoutDecisionResolver.resolve(
                            end = instructions,
                            paymentMethodType = paymentMethodType,
                        )
                        when (decision) {
                            is CheckoutDecision.Success -> successHandler.handle(
                                payment = requireNotNull(decision.payment),
                                additionalInfo = null,
                            )

                            is CheckoutDecision.Failure -> errorHandler.handle(
                                error = decision.error,
                                payment = decision.payment,
                            )
                        }
                    }

                    is ClientInstructions.Wait -> Unit
                }
            }.catch { throwable ->
                errorHandler.handle(
                    error = baseErrorResolver.resolve(throwable),
                    payment = null,
                )
            }.collect()
        }
    }

    override fun handleActivityStartEvent(params: PaymentMethodLauncherParams) {
        val event = stepUiHandlers.firstNotNullOfOrNull { it.handleActivityStartEvent(params) }
        if (event != null) {
            composerScope.launch { _uiEvent.emit(event) }
        }
    }

    override fun handleActivityResultIntent(
        params: PaymentMethodLauncherParams,
        resultCode: Int,
        intent: Intent?,
    ) {
        stepUiHandlers.any { it.handleActivityResult(params, resultCode, intent) }
        composerScope.launch { _uiEvent.emit(ComposerUiEvent.Finish) }
    }
}
