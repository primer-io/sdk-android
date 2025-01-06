package io.primer.android.paymentmethods.core.ui.navigation

class PaymentMethodNavigationFactoryRegistry {
    private val factories: MutableMap<String, Class<out PaymentMethodNavigationHandlerFactory>> =
        mutableMapOf()

    fun create(paymentMethodType: String): PaymentMethodNavigationHandler? {
        return factories[paymentMethodType]?.getDeclaredConstructor()?.newInstance()?.create()
    }

    fun register(
        type: String,
        factory: Class<out PaymentMethodNavigationHandlerFactory>,
    ) {
        factories[type] = factory
    }

    fun unregister(type: String) {
        factories.remove(type)
    }
}
