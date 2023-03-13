package io.primer.android.components.presentation.paymentMethods.nativeUi.ipay88

import io.primer.android.StateMachine
import io.primer.android.components.domain.BaseEvent
import io.primer.android.components.domain.Event
import io.primer.android.components.domain.SideEffect
import io.primer.android.components.domain.State

internal object IPay88StateMachine {

    @Suppress("LongMethod")
    fun create(initialState: State): StateMachine<State, Event, SideEffect> {
        return StateMachine.create {
            initialState(initialState)
            state<IPay88State.Idle> {
                on<IPay88Event.OnLoadConfiguration> {
                    transitionTo(
                        IPay88State.LoadConfiguration(it.paymentMethodType),
                        IPaySideEffect.LoadConfiguration
                    )
                }
                on<IPay88Event.OnError> {
                    transitionTo(
                        IPay88State.Idle,
                        IPaySideEffect.HandleError
                    )
                }
            }
            state<IPay88State.LoadConfiguration> {
                on<IPay88Event.OnConfigurationLoaded> {
                    transitionTo(IPay88State.Tokenizing, IPaySideEffect.Tokenize)
                }
                on<IPay88Event.OnError> {
                    transitionTo(
                        IPay88State.Idle,
                        IPaySideEffect.HandleError
                    )
                }
            }
            state<IPay88State.Tokenizing> {
                on<IPay88Event.OnTokenized> {
                    transitionTo(
                        IPay88State.Idle,
                        IPaySideEffect.HandleTokenized
                    )
                }
                on<IPay88Event.OnError> {
                    transitionTo(
                        IPay88State.Idle,
                        IPaySideEffect.HandleError
                    )
                }
            }
            state<IPay88State.StartRedirect> {
                on<IPay88Event.OnRedirect> {
                    transitionTo(
                        IPay88State.Redirect(
                            it.statusUrl,
                            it.paymentMethodType,
                            it.iPay88PaymentMethodId,
                            it.iPay88ActionType
                        ),
                        IPaySideEffect.NavigateToIPayScreen
                    )
                }
            }
            state<IPay88State.Redirect> {
                on<BaseEvent.OnResult> {
                    transitionTo(
                        IPay88State.HandlingResult,
                        IPaySideEffect.HandleResult
                    )
                }
                on<IPay88Event.OnError> {
                    transitionTo(IPay88State.Idle, IPaySideEffect.HandleError)
                }
            }
            state<IPay88State.HandlingResult> {
                on<IPay88Event.OnStartPolling> {
                    transitionTo(IPay88State.StartPolling, IPaySideEffect.StartPolling)
                }
                on<IPay88Event.OnCancel> {
                    transitionTo(IPay88State.Idle, IPaySideEffect.HandleCancel)
                }
                on<IPay88Event.OnError> {
                    transitionTo(IPay88State.Idle, IPaySideEffect.HandleError)
                }
            }
            state<IPay88State.StartPolling> {
                on<IPay88Event.OnFinished> {
                    transitionTo(IPay88State.Idle, IPaySideEffect.HandleFinished)
                }
                on<IPay88Event.OnError> {
                    transitionTo(IPay88State.Idle, IPaySideEffect.HandleError)
                }
            }
        }
    }
}

internal sealed class IPay88State : State {
    object Idle : IPay88State()
    data class LoadConfiguration(val paymentMethodType: String) : IPay88State()
    object Tokenizing : IPay88State()
    data class StartRedirect(
        val paymentMethodType: String,
        val statusUrl: String,
        val iPayPaymentId: String,
        val iPayMethod: Int,
        val merchantCode: String,
        val actionType: String,
        val amount: String,
        val referenceNumber: String,
        val prodDesc: String,
        val currencyCode: String?,
        val countryCode: String?,
        val customerName: String?,
        val customerEmail: String?,
        val remark: String?,
        val backendCallbackUrl: String,
        val deeplinkUrl: String,
    ) : IPay88State()

    data class Redirect(
        val statusUrl: String,
        val paymentMethodType: String,
        val iPay88PaymentMethodId: String,
        val iPay88ActionType: String,
    ) : IPay88State()

    object HandlingResult : IPay88State()
    object StartPolling : IPay88State()
}

internal sealed class IPay88Event : Event {
    data class OnLoadConfiguration(val paymentMethodType: String) : IPay88Event()
    data class OnConfigurationLoaded(
        val paymentMethodType: String,
        val paymentMethodConfigId: String,
        val locale: String,
    ) : IPay88Event()

    object OnTokenized : IPay88Event()
    data class OnRedirect(
        val paymentMethodType: String,
        val statusUrl: String,
        val iPay88PaymentMethodId: String,
        val iPayMethod: Int,
        val merchantCode: String,
        val iPay88ActionType: String,
        val amount: String,
        val referenceNumber: String,
        val prodDesc: String,
        val currencyCode: String?,
        val countryCode: String?,
        val customerName: String?,
        val customerEmail: String?,
        val remark: String?,
        val backendCallbackUrl: String,
        val deeplinkUrl: String,
    ) : IPay88Event()

    data class OnStartPolling(
        val statusUrl: String,
        val paymentMethodType: String,
    ) : IPay88Event()

    object OnCancel : IPay88Event()
    object OnError : IPay88Event()
    object OnFinished : IPay88Event()
}

internal sealed class IPaySideEffect : SideEffect {
    object LoadConfiguration : IPaySideEffect()
    object NavigateToIPayScreen : IPaySideEffect()
    object Tokenize : IPaySideEffect()
    object HandleTokenized : IPaySideEffect()
    object StartPolling : IPaySideEffect()
    object HandleResult : IPaySideEffect()
    object HandleError : IPaySideEffect()
    object HandleCancel : IPaySideEffect()
    object HandleFinished : IPaySideEffect()
    object None : IPaySideEffect()
}
