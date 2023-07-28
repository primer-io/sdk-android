package io.primer.android.components.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import io.primer.android.PrimerSessionIntent
import io.primer.android.StateMachine
import io.primer.android.components.domain.DefaultPaymentMethodStateMachineFactory
import io.primer.android.components.domain.Event
import io.primer.android.components.domain.SideEffect
import io.primer.android.components.domain.State
import io.primer.android.components.ui.activity.PaymentMethodRedirectLauncherParams
import io.primer.android.data.configuration.models.PaymentMethodImplementationType
import io.primer.android.utils.SingleLiveEvent
import kotlin.properties.Delegates

internal abstract class NativeUIHeadlessViewModel(private val savedStateHandle: SavedStateHandle) :
    ViewModel() {

    protected var stateMachine by Delegates.notNull<StateMachine<State, Event, SideEffect>>()
    protected var sessionIntent by Delegates.notNull<PrimerSessionIntent>()
    protected abstract val initialState: State

    protected var currentState: State
        get() = savedStateHandle.get<State>(SAVED_CURRENT_STATE_KEY) ?: initialState
        set(value) {
            savedStateHandle[SAVED_CURRENT_STATE_KEY] = value
        }

    protected val _startActivityEvent: SingleLiveEvent<PaymentMethodRedirectLauncherParams> =
        SingleLiveEvent()
    val startActivityEvent: LiveData<PaymentMethodRedirectLauncherParams> = _startActivityEvent

    protected val _finishActivityEvent: SingleLiveEvent<Unit> = SingleLiveEvent()
    val finishActivityEvent: LiveData<Unit> = _finishActivityEvent

    open fun initialize(
        paymentMethodImplementationType: PaymentMethodImplementationType,
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
        initialState: State? = null
    ) {
        stateMachine =
            DefaultPaymentMethodStateMachineFactory().create(
                paymentMethodImplementationType,
                paymentMethodType,
                sessionIntent,
                initialState ?: currentState
            )
        this.sessionIntent = sessionIntent
    }

    open fun start(paymentMethodType: String, sessionIntent: PrimerSessionIntent) = Unit

    abstract fun onEvent(e: Event)

    private companion object {
        const val SAVED_CURRENT_STATE_KEY = "CURRENT_STATE"
    }
}
