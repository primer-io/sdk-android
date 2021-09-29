package io.primer.android.payment

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

/**
 * Holds all available [PaymentMethodDescriptorFactory]. At run-time, when the SDK is being set up
 * and initialized, all [PaymentMethod]s are requested to register their [PaymentMethodDescriptorFactory]
 * with this registry, doing so through their [PaymentMethodModule].
 *
 * **You should only ever need an instance of this class and it should be a singleton**.
 *
 * @see
 */
class PaymentMethodDescriptorFactoryRegistry(
    private val paymentMethodCheckers: PaymentMethodCheckerRegistry,
) {

    private val factories: MutableMap<String, PaymentMethodDescriptorFactory> = mutableMapOf()

    fun create(
        localConfig: PrimerConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
    ): PaymentMethodDescriptor? =
        factories[paymentMethodRemoteConfig.type]?.create(
            localConfig = localConfig,
            paymentMethodRemoteConfig = paymentMethodRemoteConfig,
            paymentMethod = paymentMethod,
            paymentMethodCheckers = paymentMethodCheckers
        )

    fun register(id: String, factory: PaymentMethodDescriptorFactory) {
        factories[id] = factory
    }

    fun unregister(id: String) {
        factories.remove(id)
    }

    operator fun get(id: String): PaymentMethodDescriptorFactory? = factories[id]
}
