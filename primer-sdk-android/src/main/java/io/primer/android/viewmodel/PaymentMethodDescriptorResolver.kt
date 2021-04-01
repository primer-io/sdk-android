package io.primer.android.viewmodel

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactory
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class PaymentMethodDescriptorResolver(
    private val configured: List<PaymentMethod>,
    private val paymentMethodRemoteConfigs: List<PaymentMethodRemoteConfig>,
    private val paymentMethodDescriptorFactory: PaymentMethodDescriptorFactory,
) {

    fun resolve(viewModel: PrimerViewModel): List<PaymentMethodDescriptor> {
        val list = ArrayList<PaymentMethodDescriptor>()

        paymentMethodRemoteConfigs.forEach { paymentMethodRemoteConfig ->
            configured
                .find { it.identifier == paymentMethodRemoteConfig.type }
                ?.let {
                    paymentMethodDescriptorFactory
                        .create(config = paymentMethodRemoteConfig, options = it, viewModel = viewModel)
                        ?.let { paymentMethodDescriptor -> list.add(paymentMethodDescriptor) }
                }
        }

        return list
    }
}
