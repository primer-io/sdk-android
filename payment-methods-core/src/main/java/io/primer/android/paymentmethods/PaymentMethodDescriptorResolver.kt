package io.primer.android.paymentmethods

import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig

internal interface PaymentMethodDescriptorResolver {
    suspend fun resolve(remotePaymentMethods: List<PaymentMethodConfigDataResponse>): List<PaymentMethodDescriptor> =
        emptyList()
}

class PrimerPaymentMethodDescriptorResolver(
    private val localConfig: PrimerConfig,
    private val localPaymentMethods: List<PaymentMethod>,
    private val paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry,
    private val availabilityCheckers: PaymentMethodCheckerRegistry,
) : PaymentMethodDescriptorResolver {
    override suspend fun resolve(
        remotePaymentMethods: List<PaymentMethodConfigDataResponse>,
    ): List<PaymentMethodDescriptor> {
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
            checker != null &&
                !checker.shouldPaymentMethodBeAvailable(
                    this,
                )
        }
}
