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
import io.primer.checkout.orchestrator.domain.PaymentFlowInteractor
import io.primer.checkout.orchestrator.domain.ReturnUriProvider
import io.primer.checkout.orchestrator.domain.model.CheckoutDecision
import io.primer.checkout.orchestrator.domain.model.PaymentFlowResult
import io.primer.checkout.orchestrator.domain.ui.StepUiHandler
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityResultIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.composable.ActivityStartIntentHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import io.primer.statetransport.domain.model.ClientInstructions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class BackendDrivenCheckoutComponent(
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
                handleFailure(throwable)
                return@launch
            }

            paymentFlowInteractor(
                PaymentFlowInteractor.PaymentFlowParams(
                    paymentMethodType = paymentMethodType,
                    returnUri = returnUriProvider.provide(),
                ),
            ).fold(
                onSuccess = { result ->
                    when (result) {
                        is PaymentFlowResult.Completed -> handleCheckoutEnd(
                            end = result.end,
                            paymentMethodType = paymentMethodType,
                        )

                        is PaymentFlowResult.Cancelled -> handleFailure(
                            PaymentMethodCancelledException(paymentMethodType),
                        )
                    }
                },
                onFailure = { throwable -> handleFailure(throwable) },
            )
        }
    }

    private suspend fun handleCheckoutEnd(end: ClientInstructions.End, paymentMethodType: String) {
        val decision = checkoutDecisionResolver.resolve(
            end = end,
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

    private suspend fun handleFailure(throwable: Throwable) {
        errorHandler.handle(
            error = baseErrorResolver.resolve(throwable),
            payment = null,
        )
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
