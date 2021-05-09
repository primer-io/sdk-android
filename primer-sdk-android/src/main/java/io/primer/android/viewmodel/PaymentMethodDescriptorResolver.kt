package io.primer.android.viewmodel

import io.primer.android.PaymentMethod
import io.primer.android.UXMode
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.ClientSession
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.payment.PaymentMethodType

internal interface PaymentMethodDescriptorResolver {

    suspend fun resolve(clientSession: ClientSession): List<PaymentMethodDescriptor> = emptyList()
}

internal class PrimerPaymentMethodDescriptorResolver(
    private val localConfig: CheckoutConfig,
    private val localPaymentMethods: List<PaymentMethod>,
    private val paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry,
    private val availabilityCheckers: PaymentMethodCheckerRegistry,
) : PaymentMethodDescriptorResolver {

    override suspend fun resolve(clientSession: ClientSession): List<PaymentMethodDescriptor> {
        val remotePaymentMethods = clientSession.paymentMethods
        val list = ArrayList<PaymentMethodDescriptor>()

        remotePaymentMethods.forEach { paymentMethodRemoteConfig ->
            localPaymentMethods
                .find { it.identifier == paymentMethodRemoteConfig.type }
                ?.takeUnlessUnavailable(clientSession)
                ?.let {
                    paymentMethodDescriptorFactoryRegistry
                        .create(
                            checkoutConfig = localConfig,
                            paymentMethodRemoteConfig = paymentMethodRemoteConfig,
                            paymentMethod = it,
                        )
                }
                ?.takeUnless {
                    // filter non-vaulted payment methods if ux mode is vault.
                    it.config.type == "GOOGLE_PAY" && localConfig.uxMode == UXMode.VAULT
                }
                ?.let { list.add(it) }
        }

        return list
    }

    private suspend fun PaymentMethod.takeUnlessUnavailable(clientSession: ClientSession) =
        takeUnless {
            val checker = availabilityCheckers[identifier]
            checker != null && !checker.shouldPaymentMethodBeAvailable(
                paymentMethod = this,
                clientSession = clientSession,
            )
        }
}
