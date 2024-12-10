package io.primer.android.paymentmethods

import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse

/**
 * A factory responsible for creating new [PaymentMethodDescriptor]s. Each [PaymentMethod] should
 * provide its own [PaymentMethodDescriptorFactory] via its [PaymentMethodModule]. Each factory
 * (from each [PaymentMethod]) should be registered with a [PaymentMethodDescriptorFactoryRegistry].
 * @see [PaymentMethodDescriptor]
 * @see [PaymentMethodDescriptorFactoryRegistry]
 */
interface PaymentMethodDescriptorFactory {

    fun create(
        localConfig: PrimerConfig,
        paymentMethodRemoteConfig: PaymentMethodConfigDataResponse,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry
    ): PaymentMethodDescriptor
}
