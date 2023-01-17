package io.primer.android.components.presentation.paymentMethods.nativeUi.apaya

import io.primer.android.StateMachine
import io.primer.android.components.domain.BaseEvent
import io.primer.android.components.domain.Event
import io.primer.android.components.domain.SideEffect
import io.primer.android.components.domain.State
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.models.ApayaSessionConfiguration
import io.primer.android.components.presentation.paymentMethods.nativeUi.apaya.models.ApayaPaymentModel
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.KlarnaEvent

internal object ApayaStateMachine {

    @Suppress("LongMethod")
    fun create(initialState: State): StateMachine<State, Event, SideEffect> {
        return StateMachine.create {
            initialState(initialState)
            state<ApayaState.Idle> {
                on<ApayaEvent.OnLoadConfiguration> {
                    transitionTo(ApayaState.LoadingConfiguration, ApayaSideEffect.LoadConfiguration)
                }
                on<ApayaEvent.OnError> {
                    transitionTo(
                        ApayaState.Idle,
                        ApayaSideEffect.HandleError
                    )
                }
            }
            state<ApayaState.LoadingConfiguration> {
                on<ApayaEvent.OnCreateSession> {
                    transitionTo(ApayaState.CreatingSession, ApayaSideEffect.CreateSession)
                }
                on<ApayaEvent.OnError> {
                    transitionTo(
                        ApayaState.Idle,
                        ApayaSideEffect.HandleError
                    )
                }
            }
            state<ApayaState.CreatingSession> {
                on<ApayaEvent.OnSessionCreated> {
                    transitionTo(
                        ApayaState.SessionCreated(it.apayaPaymentData),
                        ApayaSideEffect.NavigateToApaya
                    )
                }
            }

            state<ApayaState.SessionCreated> {
                on<BaseEvent.OnResult> {
                    transitionTo(
                        ApayaState.HandlingResult(it.intent?.data?.toString().orEmpty()),
                        ApayaSideEffect.HandleResult
                    )
                }
            }
            state<ApayaState.HandlingResult> {
                on<ApayaEvent.OnRedirectUrlRetrieved> {
                    transitionTo(
                        ApayaState.TokenizeStarted,
                        ApayaSideEffect.Tokenize
                    )
                }
                on<KlarnaEvent.OnError> {
                    transitionTo(
                        ApayaState.Idle,
                        ApayaSideEffect.HandleError
                    )
                }
                on<KlarnaEvent.OnCancel> {
                    transitionTo(
                        ApayaState.Idle,
                        ApayaSideEffect.HandleCancel
                    )
                }
            }
            state<ApayaState.TokenizeStarted> {
                on<ApayaEvent.OnTokenized> {
                    transitionTo(ApayaState.Idle, ApayaSideEffect.HandleFinished)
                }
                on<ApayaEvent.OnError> {
                    transitionTo(
                        ApayaState.Idle,
                        ApayaSideEffect.HandleError
                    )
                }
            }
        }
    }
}

internal sealed class ApayaState : State {
    object Idle : ApayaState()
    object LoadingConfiguration : ApayaState()
    object CreatingSession : ApayaState()
    data class SessionCreated(val apayaPaymentData: ApayaPaymentModel) : ApayaState()
    data class HandlingResult(val redirectUrl: String) : ApayaState()
    object TokenizeStarted : ApayaState()
}

internal sealed class ApayaEvent : Event {
    object OnLoadConfiguration : ApayaEvent()
    data class OnCreateSession(val apayaConfiguration: ApayaSessionConfiguration) :
        ApayaEvent()

    data class OnSessionCreated(val apayaPaymentData: ApayaPaymentModel) : ApayaEvent()
    data class OnRedirectUrlRetrieved(val redirectUrl: String) : ApayaEvent()
    object OnCancel : ApayaEvent()
    object OnError : ApayaEvent()
    object OnTokenized : ApayaEvent()
}

internal sealed class ApayaSideEffect : SideEffect {
    object LoadConfiguration : ApayaSideEffect()
    object CreateSession : ApayaSideEffect()
    object NavigateToApaya : ApayaSideEffect()
    object Tokenize : ApayaSideEffect()
    object HandleError : ApayaSideEffect()
    object HandleCancel : ApayaSideEffect()
    object HandleFinished : ApayaSideEffect()
    object HandleResult : ApayaSideEffect()
}
