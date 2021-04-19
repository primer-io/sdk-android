package io.primer.android.viewmodel

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.ClientSession
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactory
import io.primer.android.payment.google.GooglePayBridge
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class OldPaymentMethodDescriptorResolver constructor(
    private val checkoutConfig: CheckoutConfig,
    private val configured: List<PaymentMethod>,
    private val paymentMethodRemoteConfigs: List<PaymentMethodRemoteConfig>,
    private val paymentMethodDescriptorFactory: PaymentMethodDescriptorFactory,
) {

    fun resolve(): List<PaymentMethodDescriptor> {
        val list = ArrayList<PaymentMethodDescriptor>()

        paymentMethodRemoteConfigs.forEach { paymentMethodRemoteConfig ->
            configured
                .find { it.identifier == paymentMethodRemoteConfig.type }
                ?.let {
                    paymentMethodDescriptorFactory
                        .create(
                            checkoutConfig = checkoutConfig,
                            paymentMethodRemoteConfig = paymentMethodRemoteConfig,
                            paymentMethod = it,
                        )
                        ?.let { paymentMethodDescriptor -> list.add(paymentMethodDescriptor) }
                }
        }

        return list
    }
}

internal interface PaymentMethodChecker {

    suspend fun shouldPaymentMethodBeAvailable(
        paymentMethod: PaymentMethod,
        clientSession: ClientSession,
    ): Boolean
}

internal class GooglePayPaymentMethodChecker constructor(
    private val googlePayBridge: GooglePayBridge,
) : PaymentMethodChecker {

    override suspend fun shouldPaymentMethodBeAvailable(
        paymentMethod: PaymentMethod,
        clientSession: ClientSession,
    ): Boolean {
        val googlePay = paymentMethod as PaymentMethod.GooglePay
        val gatewayMerchantId = clientSession.paymentMethods
            .find { it.type == googlePay.identifier }
            ?.options?.get("merchantId")?.toString()
            ?.replace("\"", "") // FIXME issue with kotlin serialization here
            ?: return false // TODO log missing value

        return googlePayBridge.checkIfIsReadyToPay(
            gatewayMerchantId = gatewayMerchantId,
            allowedCardNetworks = googlePay.allowedCardNetworks,
            allowedCardAuthMethods = googlePay.allowedCardAuthMethods,
            billingAddressRequired = googlePay.billingAddressRequired
        )
    }
}

internal interface PaymentMethodCheckerRegistrar {

    val checkers: Map<String, PaymentMethodChecker>

    fun register(id: String, checker: PaymentMethodChecker)
    fun unregister(id: String)

    operator fun get(id: String): PaymentMethodChecker? = checkers[id]
}

internal object PrimerPaymentMethodCheckerRegistrar : PaymentMethodCheckerRegistrar {

    private val _checkers: MutableMap<String, PaymentMethodChecker> = mutableMapOf()
    override val checkers: Map<String, PaymentMethodChecker> = _checkers

    override fun register(id: String, checker: PaymentMethodChecker) {
        _checkers[id] = checker
    }

    override fun unregister(id: String) {
        _checkers.remove(id)
    }
}

internal interface PaymentMethodDescriptorResolver {

    suspend fun resolve(
//        remotePaymentMethods: List<PaymentMethodRemoteConfig>,
        clientSession: ClientSession,
    ): List<PaymentMethodDescriptor> = emptyList()
}

internal class PrimerPaymentMethodDescriptorResolver constructor(
    private val localConfig: CheckoutConfig,
    private val localPaymentMethods: List<PaymentMethod>,
    private val paymentMethodDescriptorFactory: PaymentMethodDescriptorFactory,
    private val availabilityCheckers: PaymentMethodCheckerRegistrar,
) : PaymentMethodDescriptorResolver {

    //    override suspend fun resolve(remotePaymentMethods: List<PaymentMethodRemoteConfig>): List<PaymentMethodDescriptor> {
    override suspend fun resolve(clientSession: ClientSession): List<PaymentMethodDescriptor> {
        val remotePaymentMethods = clientSession.paymentMethods
        val list = ArrayList<PaymentMethodDescriptor>()

        remotePaymentMethods.forEach { paymentMethodRemoteConfig ->
            localPaymentMethods
                .find { it.identifier == paymentMethodRemoteConfig.type }
                ?.takeUnless { paymentMethod: PaymentMethod ->
                    val checker = availabilityCheckers[paymentMethod.identifier]
                    checker != null && !checker.shouldPaymentMethodBeAvailable(
                        paymentMethod = paymentMethod,
                        clientSession = clientSession,
                    )
                }
                ?.let {
                    paymentMethodDescriptorFactory
                        .create(
                            checkoutConfig = localConfig,
                            paymentMethodRemoteConfig = paymentMethodRemoteConfig,
                            paymentMethod = it,
                        )
                        ?.let { paymentMethodDescriptor -> list.add(paymentMethodDescriptor) }
                }
        }

        return list
    }
}
