package io.primer.components.ui.card

import androidx.lifecycle.ViewModel
import io.primer.android.configuration.data.model.CardNetwork
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class CardViewModel : ViewModel(), CardScope {

    private val _state = MutableStateFlow<CardScope.State?>(null)
    override val state = _state.asStateFlow()

    init {
        _state.value = CardScope.State.Loading
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
}
