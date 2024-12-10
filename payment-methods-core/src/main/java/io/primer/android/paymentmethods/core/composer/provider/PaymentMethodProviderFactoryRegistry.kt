package io.primer.android.paymentmethods.core.composer.provider

import io.primer.android.PrimerSessionIntent
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer

class PaymentMethodProviderFactoryRegistry {

    private val factories: MutableMap<String, Class<out PaymentMethodComposerProvider.Factory>> =
        mutableMapOf()

    fun create(paymentMethodType: String, sessionIntent: PrimerSessionIntent): PaymentMethodComposer? {
        return factories[paymentMethodType]?.getDeclaredConstructor()?.newInstance()
            ?.create(paymentMethodType, sessionIntent)
    }

    fun register(paymentMethodType: String, factory: Class<out PaymentMethodComposerProvider.Factory>) {
        factories[paymentMethodType] = factory
    }

    fun unregister(paymentMethodType: String) {
        factories.remove(paymentMethodType)
    }
}
