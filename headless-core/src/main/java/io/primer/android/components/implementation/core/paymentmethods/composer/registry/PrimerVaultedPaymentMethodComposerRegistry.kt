package io.primer.android.components.implementation.core.paymentmethods.composer.registry

import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.registry.VaultedPaymentMethodComposerRegistry

internal class PrimerVaultedPaymentMethodComposerRegistry : VaultedPaymentMethodComposerRegistry {

    private val _composers: MutableMap<String, PaymentMethodComposer> =
        mutableMapOf()

    override val composers: Map<String, PaymentMethodComposer>
        get() = _composers

    override fun register(id: String, composer: PaymentMethodComposer) {
        _composers[id] = composer
    }

    override fun unregister(id: String) {
        _composers.remove(id)
    }
}
