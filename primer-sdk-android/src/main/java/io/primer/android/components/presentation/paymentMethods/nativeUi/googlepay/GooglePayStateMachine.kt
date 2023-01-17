package io.primer.android.components.presentation.paymentMethods.nativeUi.googlepay

import android.app.Activity
import com.google.android.gms.wallet.PaymentData
import io.primer.android.StateMachine
import io.primer.android.components.domain.BaseEvent
import io.primer.android.components.domain.Event
import io.primer.android.components.domain.SideEffect
import io.primer.android.components.domain.State

internal object GooglePayStateMachine {

    @Suppress("LongMethod")
    fun create(initialState: State): StateMachine<State, Event, SideEffect> {
        return StateMachine.create {
            initialState(initialState)
            state<GooglePayState.Idle> {
                on<GooglePayEvent.OnOpenHeadlessScreen> {
                    transitionTo(
                        GooglePayState.OpenHeadlessScreen,
                        GooglePaySideEffect.OpenHeadlessScreen
                    )
                }
            }
            state<GooglePayState.OpenHeadlessScreen> {
                on<GooglePayEvent.StartRedirect> {
                    transitionTo(GooglePayState.Redirect, GooglePaySideEffect.NavigateToGooglePay)
                }
            }
            state<GooglePayState.Redirect> {
                on<BaseEvent.OnResult> {
                    transitionTo(
                        GooglePayState.HandlingResult,
                        GooglePaySideEffect.HandleResult
                    )
                }
            }
            state<GooglePayState.HandlingResult> {
                on<GooglePayEvent.OnTokenizeStart> {
                    transitionTo(
                        GooglePayState.TokenizeStarted,
                        GooglePaySideEffect.Tokenize
                    )
                }
                on<GooglePayEvent.OnError> {
                    transitionTo(
                        GooglePayState.Idle,
                        GooglePaySideEffect.HandleError
                    )
                }
                on<GooglePayEvent.OnCancel> {
                    transitionTo(
                        GooglePayState.Idle,
                        GooglePaySideEffect.HandleCancel
                    )
                }
            }
            state<GooglePayState.TokenizeStarted> {
                on<GooglePayEvent.OnTokenized> {
                    transitionTo(GooglePayState.Idle, GooglePaySideEffect.HandleFinished)
                }
                on<GooglePayEvent.OnError> {
                    transitionTo(
                        GooglePayState.Idle,
                        GooglePaySideEffect.HandleError
                    )
                }
            }
        }
    }
}

internal sealed class GooglePayState : State {
    object Idle : GooglePayState()
    object OpenHeadlessScreen : GooglePayState()
    object Redirect : GooglePayState()
    object HandlingResult : GooglePayState()
    object TokenizeStarted : GooglePayState()
}

internal sealed class GooglePayEvent : Event {
    object OnOpenHeadlessScreen : GooglePayEvent()
    class StartRedirect(val activity: Activity) : GooglePayEvent()
    data class OnTokenizeStart(val paymentData: PaymentData?) : GooglePayEvent()
    object OnTokenized : GooglePayEvent()
    object OnError : GooglePayEvent()
    object OnCancel : GooglePayEvent()
}

internal sealed class GooglePaySideEffect : SideEffect {
    object OpenHeadlessScreen : GooglePaySideEffect()
    object NavigateToGooglePay : GooglePaySideEffect()
    object Tokenize : GooglePaySideEffect()
    object HandleResult : GooglePaySideEffect()
    object HandleFinished : GooglePaySideEffect()
    object HandleError : GooglePaySideEffect()
    object HandleCancel : GooglePaySideEffect()
}
