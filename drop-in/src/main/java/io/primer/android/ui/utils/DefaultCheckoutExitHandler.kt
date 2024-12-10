package io.primer.android.ui.utils

import io.primer.android.payments.core.helpers.CheckoutExitHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach

internal class DefaultCheckoutExitHandler(private val onExit: () -> Unit) : CheckoutExitHandler {

    private val _checkoutExited = MutableSharedFlow<Unit>(replay = 1)

    override val checkoutExited: Flow<Unit> = _checkoutExited.distinctUntilChanged().onEach {
        onExit()
    }

    override fun handle() {
        _checkoutExited.tryEmit(Unit)
    }
}
