package io.primer.android.payment

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

/**
 * A factory responsible for creating new [PaymentMethodDescriptor]s. Each [PaymentMethod] should
 * provide its own [PaymentMethodDescriptorFactory] via its [PaymentMethodModule]. Each factory
 * (from each [PaymentMethod]) should be registered with a [PaymentMethodDescriptorFactoryRegistry].
 * @see [PaymentMethodDescriptor]
 * @see [PaymentMethodDescriptorFactoryRegistry]
 * @see [PrimerPaymentMethodDescriptorResolver]
 */
interface PaymentMethodDescriptorFactory {

    fun create(
        localConfig: PrimerConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): PaymentMethodDescriptor
}
