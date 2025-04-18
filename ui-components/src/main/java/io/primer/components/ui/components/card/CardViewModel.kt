package io.primer.components.ui.components.card

import androidx.lifecycle.ViewModel
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.components.PaymentMethodScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class CardViewModel : ViewModel(), CardPaymentMethodScope {

    private val _state = MutableStateFlow<PaymentMethodScope.State?>(null)
    override val state = _state.asStateFlow()

    init {
        _state.value = State.Loading
    }

    override fun onCardNumberChange(value: String) {
        // TODO
    }

    override fun onCardExpirationChange(value: String) {
        // TODO
    }

    override fun onCvvChange(value: String) {
        // TODO
    }

    override fun onCardholderNameChange(value: String) {
        // TODO
    }

    override fun onCardNetworkChange(value: CardNetwork.Type) {
        // TODO
    }

    override fun submit() {
        // TODO
    }

    override fun cancel() {
        // TODO
    }

    sealed interface State : PaymentMethodScope.State {

        data object Loading : State

        data class Loaded(
            val cardNumber: String,
            val cvv: String,
            val expiration: String,
            val cardholderName: String,
        ) : State

        data object Error : State
    }
}
