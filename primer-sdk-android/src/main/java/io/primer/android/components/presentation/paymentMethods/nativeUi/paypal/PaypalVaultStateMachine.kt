package io.primer.android.components.presentation.paymentMethods.nativeUi.paypal

import io.primer.android.StateMachine
import io.primer.android.components.domain.BaseEvent
import io.primer.android.components.domain.Event
import io.primer.android.components.domain.SideEffect
import io.primer.android.components.domain.State
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalConfirmBillingAgreement
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalVaultConfiguration

internal object PaypalVaultStateMachine {

    @Suppress("LongMethod")
    fun create(initialState: State): StateMachine<State, Event, SideEffect> {
        return StateMachine.create {
            initialState(initialState)
            state<PaypalVaultState.Idle> {
                on<PaypalVaultEvent.OnLoadConfiguration> {
                    transitionTo(
                        PaypalVaultState.LoadConfiguration,
                        PaypalVaultSideEffect.LoadConfiguration
                    )
                }
                on<PaypalVaultEvent.OnError> {
                    transitionTo(
                        PaypalVaultState.Idle,
                        PaypalVaultSideEffect.HandleError
                    )
                }
            }
            state<PaypalVaultState.LoadConfiguration> {
                on<PaypalVaultEvent.OnCreateBillingAgreement> {
                    transitionTo(
                        PaypalVaultState.CreatingBillingAgreement,
                        PaypalVaultSideEffect.CreateBillingAgreement
                    )
                }
            }
            state<PaypalVaultState.CreatingBillingAgreement> {
                on<PaypalVaultEvent.OnBillingAgreementCreated> {
                    transitionTo(
                        PaypalVaultState.Redirect(
                            it.paymentMethodConfigId,
                            it.successUrl,
                            it.cancelUrl
                        ),
                        PaypalVaultSideEffect.NavigateToPaypal
                    )
                }
                on<PaypalEvent.OnError> {
                    transitionTo(
                        PaypalVaultState.Idle,
                        PaypalVaultSideEffect.HandleError
                    )
                }
            }
            state<PaypalVaultState.Redirect> {
                on<BaseEvent.OnBrowserResult> {
                    transitionTo(
                        PaypalVaultState.HandlingResult(this.paymentMethodConfigId),
                        PaypalVaultSideEffect.HandleResult
                    )
                }
            }

            state<PaypalVaultState.HandlingResult> {
                on<PaypalVaultEvent.OnConfirmBillingAgreement> {
                    transitionTo(
                        PaypalVaultState.ConfirmBillingAgreement(this.paymentMethodConfigId),
                        PaypalVaultSideEffect.ConfirmBillingAgreement
                    )
                }
                on<PaypalEvent.OnError> {
                    transitionTo(
                        PaypalVaultState.Idle,
                        PaypalVaultSideEffect.HandleError
                    )
                }
            }

            state<PaypalVaultState.ConfirmBillingAgreement> {
                on<PaypalVaultEvent.OnBillingAgreementConfirmed> {
                    transitionTo(PaypalVaultState.Tokenizing, PaypalVaultSideEffect.Tokenize)
                }
                on<PaypalEvent.OnError> {
                    transitionTo(
                        PaypalVaultState.Idle,
                        PaypalVaultSideEffect.HandleError
                    )
                }
            }

            state<PaypalVaultState.Tokenizing> {
                on<PaypalVaultEvent.OnTokenized> {
                    transitionTo(PaypalVaultState.Idle, PaypalVaultSideEffect.HandleFinished)
                }
                on<PaypalEvent.OnError> {
                    transitionTo(
                        PaypalVaultState.Idle,
                        PaypalVaultSideEffect.HandleError
                    )
                }
            }
        }
    }
}

internal sealed class PaypalVaultState : State {
    object Idle : PaypalVaultState()
    object LoadConfiguration : PaypalVaultState()
    object CreatingBillingAgreement : PaypalVaultState()
    data class Redirect(
        val paymentMethodConfigId: String,
        val successUrl: String,
        val cancelUrl: String
    ) : PaypalVaultState()

    data class HandlingResult(val paymentMethodConfigId: String) : PaypalVaultState()
    data class ConfirmBillingAgreement(val paymentMethodConfigId: String) : PaypalVaultState()
    object Tokenizing : PaypalVaultState()
}

internal sealed class PaypalVaultEvent : Event {
    object OnLoadConfiguration : PaypalVaultEvent()
    data class OnCreateBillingAgreement(val configuration: PaypalVaultConfiguration) :
        PaypalVaultEvent()

    data class OnBillingAgreementCreated(
        val approvalUrl: String,
        val paymentMethodConfigId: String,
        val successUrl: String,
        val cancelUrl: String
    ) : PaypalVaultEvent()

    data class OnConfirmBillingAgreement(val paymentMethodConfigId: String, val orderId: String?) :
        PaypalVaultEvent()

    data class OnBillingAgreementConfirmed(
        val paypalConfirmBillingAgreement: PaypalConfirmBillingAgreement
    ) : PaypalVaultEvent()

    object OnTokenized : PaypalVaultEvent()
    object OnError : PaypalVaultEvent()
}

internal sealed class PaypalVaultSideEffect : SideEffect {
    object LoadConfiguration : PaypalVaultSideEffect()
    object CreateBillingAgreement : PaypalVaultSideEffect()
    object NavigateToPaypal : PaypalVaultSideEffect()
    object ConfirmBillingAgreement : PaypalVaultSideEffect()
    object Tokenize : PaypalVaultSideEffect()
    object HandleError : PaypalVaultSideEffect()
    object HandleFinished : PaypalVaultSideEffect()
    object HandleResult : PaypalVaultSideEffect()
}
