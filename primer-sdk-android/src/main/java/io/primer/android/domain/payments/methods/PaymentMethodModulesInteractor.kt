package io.primer.android.domain.payments.methods

import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.configuration.models.isAvailableOnHUC
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.exception.MissingPaymentMethodException
import io.primer.android.domain.exception.UnsupportedPaymentIntentException
import io.primer.android.domain.payments.methods.repository.PaymentMethodsRepository
import io.primer.android.domain.session.repository.ConfigurationRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import io.primer.android.domain.payments.methods.models.PaymentModuleParams
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorMapping
import io.primer.android.payment.VaultCapability
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import org.koin.core.component.KoinApiExtension

@ExperimentalCoroutinesApi
@KoinApiExtension
internal class PaymentMethodModulesInteractor(
    private val paymentMethodsRepository: PaymentMethodsRepository,
    private val configurationRepository: ConfigurationRepository,
    private val config: PrimerConfig,
    private val eventDispatcher: EventDispatcher,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    private val logger: Logger,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseInteractor<PaymentMethodModulesInteractor.PaymentDescriptorsHolder, PaymentModuleParams>() {

    override fun execute(params: PaymentModuleParams) =
        paymentMethodsRepository.getPaymentMethodDescriptors()
            .combine(
                configurationRepository.fetchConfiguration(true)
                    .map { it.paymentMethods.map { it.type.name } }
            ) { descriptors, paymentMethods -> Pair(descriptors, paymentMethods) }
            .onStart {
                if (params.sendStartEvent) {
                    eventDispatcher.dispatchEvent(
                        CheckoutEvent.PreparationStarted(
                            config.intent.paymentMethod ?: PaymentMethodType.UNKNOWN
                        )
                    )
                }
            }
            .mapLatest { paymentMethodData ->
                val descriptors = paymentMethodData.first.filter { isValidPaymentDescriptor(it) }
                if (config.isStandalonePaymentMethod) {
                    val availablePaymentMethods = paymentMethodData.second
                    val paymentMethod = requireNotNull(config.intent.paymentMethod)
                    // if the payment method is not present or not present after filtering
                    if (availablePaymentMethods.contains(paymentMethod.name).not()) {
                        throw MissingPaymentMethodException(paymentMethod)
                    } else if (
                        descriptors.none {
                            it.config.type == paymentMethod
                        }
                    ) {
                        throw UnsupportedPaymentIntentException(
                            paymentMethod,
                            config.intent.paymentMethodIntent
                        )
                    } else if (config.settings.fromHUC && paymentMethod.isAvailableOnHUC().not()) {
                        throw UnsupportedPaymentMethodException(paymentMethod)
                    }
                }
                descriptors
            }
            .mapLatest { descriptors ->
                val mapping = PaymentMethodDescriptorMapping(descriptors)
                // we get the descriptor we need for standalone PM
                if (config.isStandalonePaymentMethod) {
                    val paymentMethod = requireNotNull(config.intent.paymentMethod)
                    val descriptor = mapping.getDescriptorFor(paymentMethod)
                    descriptor?.let {
                        PaymentDescriptorsHolder(descriptors, descriptor)
                    } ?: throw MissingPaymentMethodException(paymentMethod)
                } else {
                    PaymentDescriptorsHolder(descriptors)
                }
            }.flowOn(dispatcher)
            .catch {
                baseErrorEventResolver.resolve(it, ErrorMapperType.PAYMENT_METHODS)
                logger.error(it.message.orEmpty(), it)
            }

    private fun isValidPaymentDescriptor(descriptor: PaymentMethodDescriptor) = (
        descriptor.vaultCapability == VaultCapability.VAULT_ONLY &&
            config.paymentMethodIntent.isVault
        ) || (
        descriptor.vaultCapability == VaultCapability.SINGLE_USE_ONLY &&
            config.paymentMethodIntent.isCheckout
        ) ||
        descriptor.vaultCapability == VaultCapability.SINGLE_USE_AND_VAULT

    data class PaymentDescriptorsHolder(
        val descriptors: List<PaymentMethodDescriptor>,
        val selectedPaymentMethodDescriptor: PaymentMethodDescriptor? = null,
    )
}
