package io.primer.android.components.presentation.paymentMethods.nativeUi.webRedirect

import io.primer.android.StateMachine
import io.primer.android.components.domain.BaseEvent
import io.primer.android.components.domain.Event
import io.primer.android.components.domain.SideEffect
import io.primer.android.components.domain.State

internal object AsyncStateMachine {

    @Suppress("LongMethod")
    fun create(initialState: State): StateMachine<State, Event, SideEffect> {
        return StateMachine.create {
            initialState(initialState)
            state<AsyncState.Idle> {
                on<AsyncEvent.OnLoadConfiguration> {
                    transitionTo(
                        AsyncState.LoadConfiguration(it.paymentMethodType),
                        AsyncSideEffect.LoadConfiguration
                    )
                }
                on<AsyncEvent.OnError> {
                    transitionTo(
                        AsyncState.Idle,
                        AsyncSideEffect.HandleError
                    )
                }
            }
            state<AsyncState.LoadConfiguration> {
                on<AsyncEvent.OnConfigurationLoaded> {
                    transitionTo(AsyncState.Tokenizing, AsyncSideEffect.Tokenize)
                }
                on<AsyncEvent.OnError> {
                    transitionTo(
                        AsyncState.Idle,
                        AsyncSideEffect.HandleError
                    )
                }
            }
            state<AsyncState.Tokenizing> {
                on<AsyncEvent.OnTokenized> {
                    transitionTo(
                        AsyncState.Idle,
                        AsyncSideEffect.HandleTokenized
                    )
                }
                on<AsyncEvent.OnError> {
                    transitionTo(
                        AsyncState.Idle,
                        AsyncSideEffect.HandleError
                    )
                }
            }
            state<AsyncState.StartRedirect> {
                on<AsyncEvent.OnRedirect> {
                    transitionTo(
                        AsyncState.Redirect(it.statusUrl, it.paymentMethodType),
                        AsyncSideEffect.NavigateToAsyncScreen
                    )
                }
            }
            state<AsyncState.Redirect> {
                on<BaseEvent.OnResult> {
                    transitionTo(
                        AsyncState.HandlingResult,
                        AsyncSideEffect.HandleResult
                    )
                }
                on<AsyncEvent.OnError> {
                    transitionTo(AsyncState.Idle, AsyncSideEffect.HandleError)
                }
            }
            state<AsyncState.HandlingResult> {
                on<AsyncEvent.OnStartPolling> {
                    transitionTo(AsyncState.StartPolling, AsyncSideEffect.StartPolling)
                }
                on<AsyncEvent.OnCancel> {
                    transitionTo(AsyncState.Idle, AsyncSideEffect.HandleCancel)
                }
            }
            state<AsyncState.StartPolling> {
                on<AsyncEvent.OnFinished> {
                    transitionTo(AsyncState.Idle, AsyncSideEffect.HandleFinished)
                }
                on<AsyncEvent.OnError> {
                    transitionTo(AsyncState.Idle, AsyncSideEffect.HandleError)
                }
            }
        }
    }
}

internal sealed class AsyncState : State {
    object Idle : AsyncState()
    data class LoadConfiguration(val paymentMethodType: String) : AsyncState()
    object Tokenizing : AsyncState()
    data class StartRedirect(
        val title: String,
        val paymentMethodType: String,
        val redirectUrl: String,
        val statusUrl: String,
        val returnUrl: String,
    ) : AsyncState()

    data class Redirect(val statusUrl: String, val paymentMethodType: String) : AsyncState()
    object HandlingResult : AsyncState()
    object StartPolling : AsyncState()
}

internal sealed class AsyncEvent : Event {
    data class OnLoadConfiguration(val paymentMethodType: String) : AsyncEvent()
    data class OnConfigurationLoaded(
        val paymentMethodType: String,
        val paymentMethodConfigId: String,
        val locale: String,
    ) : AsyncEvent()

    object OnTokenized : AsyncEvent()
    data class OnRedirect(
        val title: String,
        val paymentMethodType: String,
        val redirectUrl: String,
        val statusUrl: String,
        val returnUrl: String,
    ) : AsyncEvent()

    data class OnStartPolling(
        val statusUrl: String,
        val paymentMethodType: String,
    ) : AsyncEvent()

    object OnCancel : AsyncEvent()
    object OnError : AsyncEvent()
    object OnFinished : AsyncEvent()
}

internal sealed class AsyncSideEffect : SideEffect {
    object LoadConfiguration : AsyncSideEffect()
    object NavigateToAsyncScreen : AsyncSideEffect()
    object Tokenize : AsyncSideEffect()
    object HandleTokenized : AsyncSideEffect()
    object StartPolling : AsyncSideEffect()
    object HandleResult : AsyncSideEffect()
    object HandleError : AsyncSideEffect()
    object HandleCancel : AsyncSideEffect()
    object HandleFinished : AsyncSideEffect()
    object None : AsyncSideEffect()
}
