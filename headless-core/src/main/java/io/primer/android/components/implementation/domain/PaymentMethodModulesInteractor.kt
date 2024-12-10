package io.primer.android.components.implementation.domain

import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.domain.None
import io.primer.android.core.extensions.mapSuspendCatching
import io.primer.android.core.extensions.zipWith
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.exception.MissingPaymentMethodException
import io.primer.android.domain.exception.UnsupportedPaymentIntentException
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.paymentmethods.PaymentMethodDescriptor
import io.primer.android.paymentmethods.SDKCapability
import io.primer.android.paymentmethods.VaultCapability
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal class PaymentMethodModulesInteractor(
    private val paymentMethodDescriptorsRepository: PaymentMethodDescriptorsRepository,
    private val configurationRepository: ConfigurationRepository,
    private val config: PrimerConfig,
    private val baseErrorResolver: BaseErrorResolver,
    private val checkoutErrorHandler: CheckoutErrorHandler,
    private val logReporter: LogReporter,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<
    PaymentMethodModulesInteractor.PaymentDescriptorsHolder,
    None>() {

    override suspend fun performAction(params: None): Result<PaymentDescriptorsHolder> {
        return paymentMethodDescriptorsRepository.resolvePaymentMethodDescriptors()
            .zipWith(
                configurationRepository.fetchConfiguration(CachePolicy.ForceCache)
            ) { descriptors, paymentMethods ->
                Pair(
                    descriptors,
                    paymentMethods
                )
            }
            .mapSuspendCatching { paymentMethodData ->
                val descriptors = paymentMethodData.first
                if (config.isStandalonePaymentMethod) {
                    val availablePaymentMethods = paymentMethodData.second
                    val paymentMethod = requireNotNull(config.intent.paymentMethodType)
                    // if the payment method is not present or not present after filtering
                    if (availablePaymentMethods.paymentMethods.map { it.type }.contains(paymentMethod).not()) {
                        throw MissingPaymentMethodException(paymentMethod)
                    } else if (
                        descriptors.filter { isValidPaymentDescriptor(it) }.none {
                            it.config.type == paymentMethod
                        }
                    ) {
                        throw UnsupportedPaymentIntentException(
                            paymentMethod,
                            config.intent.paymentMethodIntent
                        )
                    } else if (
                        descriptors.filter { isSdkFlowSupportedPaymentDescriptor(it) }.none {
                            it.config.type == paymentMethod
                        }
                    ) {
                        throw UnsupportedPaymentMethodException(paymentMethod)
                    }
                }
                descriptors.filter {
                    (config.settings.fromHUC || isValidPaymentDescriptor(it)) &&
                        isSdkFlowSupportedPaymentDescriptor(it)
                }
            }
            .mapSuspendCatching { descriptors ->
                val mapping = PaymentMethodDescriptorMapping(descriptors)
                // we get the descriptor we need for standalone PM
                if (config.isStandalonePaymentMethod) {
                    val paymentMethod = requireNotNull(config.intent.paymentMethodType)
                    val descriptor = mapping.getDescriptorFor(paymentMethod)
                    descriptor?.let {
                        PaymentDescriptorsHolder(descriptors, descriptor)
                    } ?: throw MissingPaymentMethodException(paymentMethod)
                } else {
                    PaymentDescriptorsHolder(descriptors)
                }
            }
            .onFailure { throwable ->
                checkoutErrorHandler.handle(error = baseErrorResolver.resolve(throwable), payment = null)
                logReporter.error(throwable.message.orEmpty(), throwable = throwable)
            }
    }

    private fun isValidPaymentDescriptor(descriptor: PaymentMethodDescriptor) = (
        descriptor.vaultCapability == VaultCapability.VAULT_ONLY &&
            config.paymentMethodIntent.isVault
        ) || (
        descriptor.vaultCapability == VaultCapability.SINGLE_USE_ONLY &&
            config.paymentMethodIntent.isCheckout
        ) ||
        descriptor.vaultCapability == VaultCapability.SINGLE_USE_AND_VAULT

    private fun isSdkFlowSupportedPaymentDescriptor(descriptor: PaymentMethodDescriptor) =
        if (config.settings.fromHUC) {
            descriptor.sdkCapabilities.contains(SDKCapability.HEADLESS)
        } else {
            descriptor.sdkCapabilities.contains(SDKCapability.DROP_IN)
        }

    data class PaymentDescriptorsHolder(
        val descriptors: List<PaymentMethodDescriptor>,
        val selectedPaymentMethodDescriptor: PaymentMethodDescriptor? = null
    )
}
