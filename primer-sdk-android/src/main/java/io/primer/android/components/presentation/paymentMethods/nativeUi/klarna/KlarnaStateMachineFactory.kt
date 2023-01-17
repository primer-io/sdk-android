package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna

import io.primer.android.StateMachine
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCustomerTokenDataResponse
import io.primer.android.components.domain.BaseEvent
import io.primer.android.components.domain.Event
import io.primer.android.components.domain.SideEffect
import io.primer.android.components.domain.State
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models.KlarnaPaymentModel

internal object KlarnaStateMachine {

    @Suppress("LongMethod")
    fun create(initialState: State): StateMachine<State, Event, SideEffect> {
        return StateMachine.create {
            initialState(initialState)
            state<KlarnaState.Idle> {
                on<KlarnaEvent.OnCreateSession> {
                    transitionTo(KlarnaState.CreatingSession, KlarnaSideEffect.CreateSession)
                }
            }
            state<KlarnaState.CreatingSession> {
                on<KlarnaEvent.OnSessionCreated> {
                    transitionTo(
                        KlarnaState.SessionCreated(it.klarnaPaymentData),
                        KlarnaSideEffect.NavigateToKlarna
                    )
                }
                on<KlarnaEvent.OnError> {
                    transitionTo(KlarnaState.Idle, KlarnaSideEffect.HandleError)
                }
            }
            state<KlarnaState.SessionCreated> {
                on<BaseEvent.OnResult> {
                    transitionTo(
                        KlarnaState.HandlingResult(this.klarnaPaymentData.sessionId),
                        KlarnaSideEffect.HandleResult
                    )
                }
            }
            state<KlarnaState.HandlingResult> {
                on<KlarnaEvent.OnAuthTokenRetrieved> {
                    transitionTo(
                        KlarnaState.CreatingCustomerToken,
                        KlarnaSideEffect.CreateCustomerToken
                    )
                }
                on<KlarnaEvent.OnError> {
                    transitionTo(
                        KlarnaState.Idle,
                        KlarnaSideEffect.HandleError
                    )
                }
                on<KlarnaEvent.OnCancel> {
                    transitionTo(
                        KlarnaState.Idle,
                        KlarnaSideEffect.HandleCancel
                    )
                }
            }

            state<KlarnaState.CreatingCustomerToken> {
                on<KlarnaEvent.OnCustomerTokenRetrieved> {
                    transitionTo(KlarnaState.TokenizeStarted, KlarnaSideEffect.Tokenize)
                }
                on<KlarnaEvent.OnError> {
                    transitionTo(KlarnaState.Idle, KlarnaSideEffect.HandleError)
                }
            }

            state<KlarnaState.TokenizeStarted> {
                on<KlarnaEvent.OnTokenized> {
                    transitionTo(KlarnaState.Idle, KlarnaSideEffect.HandleFinished)
                }
                on<KlarnaEvent.OnError> {
                    transitionTo(KlarnaState.Idle, KlarnaSideEffect.HandleError)
                }
            }
        }
    }
}

internal sealed class KlarnaState : State {
    object Idle : KlarnaState()
    object CreatingSession : KlarnaState()
    data class SessionCreated(val klarnaPaymentData: KlarnaPaymentModel) : KlarnaState()
    data class HandlingResult(val sessionId: String) : KlarnaState()
    object CreatingCustomerToken : KlarnaState()
    object TokenizeStarted : KlarnaState()
}

internal sealed class KlarnaEvent : Event {
    object OnCreateSession : KlarnaEvent()
    data class OnSessionCreated(val klarnaPaymentData: KlarnaPaymentModel) : KlarnaEvent()
    data class OnAuthTokenRetrieved(val authToken: String) : KlarnaEvent()
    object OnCancel : KlarnaEvent()
    object OnError : KlarnaEvent()
    data class OnCustomerTokenRetrieved(
        val customerTokenDataResponse: CreateCustomerTokenDataResponse
    ) : KlarnaEvent()

    object OnTokenized : KlarnaEvent()
}

internal sealed class KlarnaSideEffect : SideEffect {
    object CreateSession : KlarnaSideEffect()
    object NavigateToKlarna : KlarnaSideEffect()
    object CreateCustomerToken : KlarnaSideEffect()
    object Tokenize : KlarnaSideEffect()
    object HandleError : KlarnaSideEffect()
    object HandleCancel : KlarnaSideEffect()
    object HandleFinished : KlarnaSideEffect()
    object HandleResult : KlarnaSideEffect()
}
