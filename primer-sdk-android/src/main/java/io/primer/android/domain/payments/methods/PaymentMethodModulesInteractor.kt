package io.primer.android.domain.payments.methods

import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.methods.repository.PaymentMethodDescriptorsRepository
import io.primer.android.domain.session.repository.ConfigurationRepository
import io.primer.android.logging.Logger
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.SDKCapability
import io.primer.android.payment.VaultCapability
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest

@ExperimentalCoroutinesApi
internal class PaymentMethodModulesInteractor(
    private val paymentMethodDescriptorsRepository: PaymentMethodDescriptorsRepository,
    private val configurationRepository: ConfigurationRepository,
    private val config: PrimerConfig,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    private val logger: Logger,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseFlowInteractor<
    PaymentMethodModulesInteractor.PaymentDescriptorsHolder,
    None>() {

    override fun execute(params: None) =
        paymentMethodDescriptorsRepository.resolvePaymentMethodDescriptors()
            .combine(
                configurationRepository.fetchConfiguration(true)
                    .map { it.paymentMethods }
            ) { descriptors, paymentMethods -> Pair(descriptors, paymentMethods) }
            .mapLatest { paymentMethodData ->
                val descriptors = paymentMethodData.first
                descriptors.filter {
                    (config.settings.fromHUC || isValidPaymentDescriptor(it)) &&
                        isSdkFlowSupportedPaymentDescriptor(it)
                }
            }
            .mapLatest { descriptors ->
                PaymentDescriptorsHolder(descriptors)
            }.flowOn(dispatcher)
            .catch {
                baseErrorEventResolver.resolve(it, ErrorMapperType.PAYMENT_METHODS)
                logger.error(it.message.orEmpty(), it)
            }

    fun getPaymentMethodDescriptors(): List<PaymentMethodDescriptor> {
        val descriptors = paymentMethodDescriptorsRepository.getPaymentMethodDescriptors()
        return descriptors.filter {
            (config.settings.fromHUC || isValidPaymentDescriptor(it)) &&
                isSdkFlowSupportedPaymentDescriptor(it)
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
        val selectedPaymentMethodDescriptor: PaymentMethodDescriptor? = null,
    )
}
