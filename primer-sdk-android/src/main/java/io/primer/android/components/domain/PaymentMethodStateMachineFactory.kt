package io.primer.android.components.domain

import android.content.Intent
import android.net.Uri
import io.primer.android.PrimerSessionIntent
import io.primer.android.StateMachine
import io.primer.android.components.presentation.paymentMethods.nativeUi.apaya.ApayaStateMachine
import io.primer.android.components.presentation.paymentMethods.nativeUi.googlepay.GooglePayStateMachine
import io.primer.android.components.presentation.paymentMethods.nativeUi.ipay88.IPay88StateMachine
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.KlarnaStateMachine
import io.primer.android.components.presentation.paymentMethods.nativeUi.paypal.PaypalCheckoutStateMachine
import io.primer.android.components.presentation.paymentMethods.nativeUi.paypal.PaypalVaultStateMachine
import io.primer.android.components.presentation.paymentMethods.nativeUi.webRedirect.AsyncStateMachine
import io.primer.android.data.configuration.models.PaymentMethodType
import java.io.Serializable

internal interface PaymentMethodStateMachineFactory {

    fun create(
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
        initialState: State
    ): StateMachine<State, Event, SideEffect>
}

internal class DefaultPaymentMethodStateMachineFactory : PaymentMethodStateMachineFactory {
    override fun create(
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
        initialState: State
    ): StateMachine<State, Event, SideEffect> {
        return when (paymentMethodType) {
            PaymentMethodType.GOOGLE_PAY.name -> GooglePayStateMachine.create(initialState)
            PaymentMethodType.PAYPAL.name -> {
                when (sessionIntent) {
                    PrimerSessionIntent.CHECKOUT -> PaypalCheckoutStateMachine.create(initialState)
                    PrimerSessionIntent.VAULT -> PaypalVaultStateMachine.create(initialState)
                }
            }
            PaymentMethodType.KLARNA.name -> KlarnaStateMachine.create(initialState)
            PaymentMethodType.APAYA.name -> ApayaStateMachine.create(initialState)
            PaymentMethodType.IPAY88_CARD.name -> IPay88StateMachine.create(initialState)
            else -> AsyncStateMachine.create(initialState)
        }
    }
}

internal interface State : Serializable

internal interface Event

internal sealed class BaseEvent : Event {
    data class OnResult(val intent: Intent?, val resultCode: Int) : BaseEvent()
    data class OnBrowserResult(val uri: Uri?) : BaseEvent()
}

internal interface SideEffect
