package io.primer.android.payment

import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.models.PaymentMethodRemoteConfig
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.internal.PrimerConfig
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
internal class PaymentMethodDescriptorFactoryRegistry(
    private val paymentMethodCheckers: PaymentMethodCheckerRegistry,
) {

    private val factories: MutableMap<PaymentMethodType, PaymentMethodDescriptorFactory> =
        mutableMapOf()

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

    fun register(type: PaymentMethodType, factory: PaymentMethodDescriptorFactory) {
        factories[type] = factory
    }

    fun unregister(type: PaymentMethodType) {
        factories.remove(type)
    }

    operator fun get(type: PaymentMethodType): PaymentMethodDescriptorFactory? = factories[type]
}
