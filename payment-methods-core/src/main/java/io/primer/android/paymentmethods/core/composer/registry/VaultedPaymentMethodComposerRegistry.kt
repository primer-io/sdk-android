package io.primer.android.paymentmethods.core.composer.registry

import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer

interface VaultedPaymentMethodComposerRegistry {

    val composers: Map<String, PaymentMethodComposer>

    fun register(id: String, composer: PaymentMethodComposer)
    fun unregister(id: String)

    operator fun get(id: String): PaymentMethodComposer? = composers[id]
}
