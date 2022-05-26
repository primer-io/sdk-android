package io.primer.android.viewmodel

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry

internal interface PaymentMethodDescriptorResolver {

    suspend fun resolve(remotePaymentMethods: List<PaymentMethodRemoteConfig>):
        List<PaymentMethodDescriptor> = emptyList()
}

internal class PrimerPaymentMethodDescriptorResolver(
    private val localConfig: PrimerConfig,
    private val localPaymentMethods: List<PaymentMethod>,
    private val paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry,
    private val availabilityCheckers: PaymentMethodCheckerRegistry,
) : PaymentMethodDescriptorResolver {

    override suspend fun resolve(remotePaymentMethods: List<PaymentMethodRemoteConfig>):
        List<PaymentMethodDescriptor> {
        val list = ArrayList<PaymentMethodDescriptor>()
        remotePaymentMethods.forEach { paymentMethodRemoteConfig ->
            localPaymentMethods
                .find { it.type == paymentMethodRemoteConfig.type }
                ?.takeUnlessUnavailable()
                ?.let {
                    paymentMethodDescriptorFactoryRegistry
                        .create(
                            localConfig = localConfig,
                            paymentMethodRemoteConfig = paymentMethodRemoteConfig,
                            paymentMethod = it,
                        )
                }
                ?.let { list.add(it) }
        }

        return list
    }

    private suspend fun PaymentMethod.takeUnlessUnavailable() =
        takeUnless {
            val checker = availabilityCheckers[it.type]
            checker != null && !checker.shouldPaymentMethodBeAvailable(
                this,
            )
        }
}
