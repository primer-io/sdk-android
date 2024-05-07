package io.primer.android.components.presentation.paymentMethods.nativeUi.paypal

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.PrimerSessionIntent
import io.primer.android.StateMachine
import io.primer.android.components.domain.BaseEvent
import io.primer.android.components.domain.Event
import io.primer.android.components.domain.State
import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.PaypalCheckoutConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.PaypalCreateOrderInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.PaypalOrderInfoInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalCheckoutConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalCreateOrderParams
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalOrderInfo
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalOrderInfoParams
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.validation.PaypalCheckoutOrderInfoValidationRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.validation.PaypalCheckoutOrderValidationRulesResolver
import io.primer.android.components.presentation.NativeUIHeadlessViewModel
import io.primer.android.components.ui.activity.BrowserLauncherParams
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.PaymentMethodCancelledException
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.resolve
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.None
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.paypal.PaypalCheckoutPaymentInstrumentParams
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
internal class PaypalCheckoutHeadlessViewModel(
    private val configurationInteractor: PaypalCheckoutConfigurationInteractor,
    private val createOrderInteractor: PaypalCreateOrderInteractor,
    private val paypalOrderInfoInteractor: PaypalOrderInfoInteractor,
    private val tokenizationInteractor: TokenizationInteractor,
    private val orderInfoValidationRulesResolver: PaypalCheckoutOrderInfoValidationRulesResolver,
    private val orderValidationRulesResolver: PaypalCheckoutOrderValidationRulesResolver,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    savedStateHandle: SavedStateHandle
) : NativeUIHeadlessViewModel(savedStateHandle), DISdkComponent {

    override val initialState: State = PaypalCheckoutState.Idle

    override fun start(paymentMethodType: String, sessionIntent: PrimerSessionIntent) {
        super.start(paymentMethodType, sessionIntent)
        onEvent(PaypalEvent.OnLoadConfiguration)
    }

    override fun onEvent(e: Event) {
        val validTransition = stateMachine.transition(e) as? StateMachine.Transition.Valid
            ?: return

        currentState = validTransition.toState

        when {
            validTransition.sideEffect is PaypalSideEffect.LoadConfiguration -> loadConfiguration()
            validTransition.sideEffect is PaypalSideEffect.CreateOrder &&
                e is PaypalEvent.OnCreateOrder -> createOrder(e.configuration)

            validTransition.sideEffect is PaypalSideEffect.NavigateToPaypal &&
                e is PaypalEvent.OnOrderCreated
            -> {
                _startActivityEvent.postValue(
                    BrowserLauncherParams(
                        e.approvalUrl,
                        Uri.parse(e.successUrl).host.orEmpty(),
                        PaymentMethodType.PAYPAL.name,
                        sessionIntent
                    )
                )
            }

            validTransition.sideEffect is PaypalSideEffect.HandleResult &&
                e is BaseEvent.OnBrowserResult &&
                validTransition.fromState is PaypalCheckoutState.Redirect
            -> handleBrowserResult(e, validTransition.fromState)

            validTransition.sideEffect is PaypalSideEffect.RetrievePaypalInfo &&
                e is PaypalEvent.OnRetrievePaypalInfo ->
                getPaypalOrderInfo(e.paymentMethodConfigId, e.orderId)

            validTransition.sideEffect is PaypalSideEffect.Tokenize &&
                e is PaypalEvent.OnPaypalInfoRetrieved -> tokenize(e.paypalOrderInfo)

            validTransition.sideEffect is PaypalSideEffect.HandleError -> {
                _finishActivityEvent.postValue(Unit)
            }

            validTransition.sideEffect is PaypalSideEffect.HandleFinished -> {
                _finishActivityEvent.postValue(Unit)
            }
        }
    }

    private fun loadConfiguration() = viewModelScope.launch {
        configurationInteractor.execute(None())
            .catch { onEvent(PaypalEvent.OnError) }
            .collect {
                onEvent(PaypalEvent.OnCreateOrder(it))
            }
    }

    private fun createOrder(configuration: PaypalCheckoutConfiguration) =
        viewModelScope.launch {
            val orderParams = PaypalCreateOrderParams(
                configuration.paymentMethodConfigId,
                configuration.amount,
                configuration.currencyCode,
                configuration.successUrl,
                configuration.cancelUrl
            )
            combine(
                orderValidationRulesResolver.resolve().rules.map {
                    flowOf(it.validate(orderParams))
                }
            ) { validationResults ->
                validationResults.forEach { result ->
                    if (result is ValidationResult.Failure) throw result.exception
                }
            }.catch { baseErrorEventResolver.resolve(it, ErrorMapperType.DEFAULT) }.flatMapLatest {
                createOrderInteractor(orderParams)
            }.catch { onEvent(PaypalEvent.OnError) }
                .collect {
                    onEvent(
                        PaypalEvent.OnOrderCreated(
                            it.approvalUrl,
                            configuration.paymentMethodConfigId,
                            it.successUrl,
                            it.cancelUrl
                        )
                    )
                }
        }

    private fun handleBrowserResult(
        event: BaseEvent.OnBrowserResult,
        state: PaypalCheckoutState.Redirect
    ) {
        when (event.uri?.buildUpon()?.clearQuery()?.build()) {
            Uri.parse(state.successUrl) -> onEvent(
                PaypalEvent.OnRetrievePaypalInfo(
                    state.paymentMethodConfigId,
                    event.uri?.getQueryParameter(TOKEN_QUERY_PARAM)
                )
            )

            else -> {
                baseErrorEventResolver.resolve(
                    PaymentMethodCancelledException(
                        PaymentMethodType.PAYPAL.name
                    ),
                    ErrorMapperType.DEFAULT
                )
                onEvent(PaypalEvent.OnError)
            }
        }
    }

    private fun getPaypalOrderInfo(paymentMethodConfigId: String, orderId: String?) =
        viewModelScope.launch {
            combine(
                orderInfoValidationRulesResolver.resolve().rules.map {
                    flowOf(it.validate(orderId))
                }
            ) { validationResults ->
                validationResults.forEach { result ->
                    if (result is ValidationResult.Failure) throw result.exception
                }
            }.catch { baseErrorEventResolver.resolve(it, ErrorMapperType.DEFAULT) }.flatMapLatest {
                paypalOrderInfoInteractor(
                    PaypalOrderInfoParams(
                        paymentMethodConfigId,
                        requireNotNull(orderId)
                    )
                )
            }.catch { onEvent(PaypalEvent.OnError) }
                .collect {
                    onEvent(PaypalEvent.OnPaypalInfoRetrieved(it))
                }
        }

    private fun tokenize(paypalOrderInfo: PaypalOrderInfo) =
        viewModelScope.launch {
            tokenizationInteractor.executeV2(
                TokenizationParamsV2(
                    PaypalCheckoutPaymentInstrumentParams(
                        paypalOrderId = paypalOrderInfo.orderId,
                        externalPayerInfoEmail = paypalOrderInfo.email,
                        externalPayerId = paypalOrderInfo.externalPayerId,
                        externalPayerFirstName = paypalOrderInfo.externalPayerFirstName,
                        externalPayerLastName = paypalOrderInfo.externalPayerLastName
                    ),
                    sessionIntent
                )
            ).catch { onEvent(PaypalEvent.OnError) }.collect { onEvent(PaypalEvent.OnTokenized) }
        }

    companion object : DISdkComponent {
        private const val TOKEN_QUERY_PARAM = "token"

        class Factory : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                // Create a SavedStateHandle for this ViewModel from extras

                return PaypalCheckoutHeadlessViewModel(
                    resolve(),
                    resolve(),
                    resolve(),
                    resolve(),
                    resolve(),
                    resolve(),
                    resolve(),
                    extras.createSavedStateHandle()
                ) as T
            }
        }
    }
}
