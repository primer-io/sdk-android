package io.primer.android.components.presentation.paymentMethods.nativeUi.paypal

import io.primer.android.StateMachine
import io.primer.android.components.domain.BaseEvent
import io.primer.android.components.domain.Event
import io.primer.android.components.domain.SideEffect
import io.primer.android.components.domain.State
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalCheckoutConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalOrderInfo

internal object PaypalCheckoutStateMachine {

    @Suppress("LongMethod")
    fun create(initialState: State): StateMachine<State, Event, SideEffect> {
        return StateMachine.create {
            initialState(initialState)
            state<PaypalCheckoutState.Idle> {
                on<PaypalEvent.OnLoadConfiguration> {
                    transitionTo(
                        PaypalCheckoutState.LoadConfiguration,
                        PaypalSideEffect.LoadConfiguration
                    )
                }
                on<PaypalEvent.OnError> {
                    transitionTo(
                        PaypalCheckoutState.Idle,
                        PaypalSideEffect.HandleError
                    )
                }
            }
            state<PaypalCheckoutState.LoadConfiguration> {
                on<PaypalEvent.OnCreateOrder> {
                    transitionTo(PaypalCheckoutState.CreatingOrder, PaypalSideEffect.CreateOrder)
                }
            }
            state<PaypalCheckoutState.CreatingOrder> {
                on<PaypalEvent.OnOrderCreated> {
                    transitionTo(
                        PaypalCheckoutState.Redirect(it.id, it.successUrl, it.cancelUrl),
                        PaypalSideEffect.NavigateToPaypal
                    )
                }
                on<PaypalEvent.OnError> {
                    transitionTo(
                        PaypalCheckoutState.Idle,
                        PaypalSideEffect.HandleError
                    )
                }
            }
            state<PaypalCheckoutState.Redirect> {
                on<BaseEvent.OnBrowserResult> {
                    transitionTo(
                        PaypalCheckoutState.HandlingResult(this.paymentMethodConfigId),
                        PaypalSideEffect.HandleResult
                    )
                }
            }

            state<PaypalCheckoutState.HandlingResult> {
                on<PaypalEvent.OnRetrievePaypalInfo> {
                    transitionTo(
                        PaypalCheckoutState.LoadingPaypalInfo(
                            this.paymentMethodConfigId,
                            it.orderId
                        ),
                        PaypalSideEffect.RetrievePaypalInfo
                    )
                }
                on<PaypalEvent.OnError> {
                    transitionTo(
                        PaypalCheckoutState.Idle,
                        PaypalSideEffect.HandleError
                    )
                }
            }

            state<PaypalCheckoutState.LoadingPaypalInfo> {
                on<PaypalEvent.OnPaypalInfoRetrieved> {
                    transitionTo(PaypalCheckoutState.Tokenizing, PaypalSideEffect.Tokenize)
                }
                on<PaypalEvent.OnError> {
                    transitionTo(
                        PaypalCheckoutState.Idle,
                        PaypalSideEffect.HandleError
                    )
                }
            }

            state<PaypalCheckoutState.Tokenizing> {
                on<PaypalEvent.OnTokenized> {
                    transitionTo(PaypalCheckoutState.Idle, PaypalSideEffect.HandleFinished)
                }
                on<PaypalEvent.OnError> {
                    transitionTo(
                        PaypalCheckoutState.Idle,
                        PaypalSideEffect.HandleError
                    )
                }
            }
        }
    }
}

internal sealed class PaypalCheckoutState : State {
    object Idle : PaypalCheckoutState()
    object LoadConfiguration : PaypalCheckoutState()
    object CreatingOrder : PaypalCheckoutState()
    data class Redirect(
        val paymentMethodConfigId: String,
        val successUrl: String,
        val cancelUrl: String
    ) : PaypalCheckoutState()

    data class HandlingResult(val paymentMethodConfigId: String) : PaypalCheckoutState()
    data class LoadingPaypalInfo(val paymentMethodConfigId: String, val orderId: String?) :
        PaypalCheckoutState()

    object Tokenizing : PaypalCheckoutState()
}

internal sealed class PaypalEvent : Event {
    object OnLoadConfiguration : PaypalEvent()
    data class OnCreateOrder(val configuration: PaypalCheckoutConfiguration) : PaypalEvent()
    data class OnOrderCreated(
        val approvalUrl: String,
        val id: String,
        val successUrl: String,
        val cancelUrl: String
    ) : PaypalEvent()

    data class OnRetrievePaypalInfo(val paymentMethodConfigId: String, val orderId: String?) :
        PaypalEvent()

    data class OnPaypalInfoRetrieved(val paypalOrderInfo: PaypalOrderInfo) : PaypalEvent()
    object OnTokenized : PaypalEvent()
    object OnError : PaypalEvent()
}

internal sealed class PaypalSideEffect : SideEffect {
    object LoadConfiguration : PaypalSideEffect()
    object CreateOrder : PaypalSideEffect()
    object NavigateToPaypal : PaypalSideEffect()
    object RetrievePaypalInfo : PaypalSideEffect()
    object Tokenize : PaypalSideEffect()
    object HandleError : PaypalSideEffect()
    object HandleFinished : PaypalSideEffect()
    object HandleResult : PaypalSideEffect()
    object None : PaypalSideEffect()
}
