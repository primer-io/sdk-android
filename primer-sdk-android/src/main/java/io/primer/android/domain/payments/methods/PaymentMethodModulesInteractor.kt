package io.primer.android.domain.payments.methods

import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.payments.methods.repository.PaymentMethodsRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.extensions.toCheckoutErrorEvent
import io.primer.android.logging.Logger
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorMapping
import io.primer.android.payment.VaultCapability
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import org.koin.core.component.KoinApiExtension

@ExperimentalCoroutinesApi
@KoinApiExtension
internal class PaymentMethodModulesInteractor(
    private val paymentMethodsRepository: PaymentMethodsRepository,
    private val config: PrimerConfig,
    private val eventDispatcher: EventDispatcher,
    private val logger: Logger,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseInteractor<PaymentMethodModulesInteractor.PaymentDescriptorsHolder, None>() {

    override fun execute(params: None) =
        paymentMethodsRepository.getPaymentMethodDescriptors()
            .onStart { eventDispatcher.dispatchEvent(CheckoutEvent.PreparationStarted) }
            .mapLatest { descriptors -> descriptors.filter { isValidPaymentDescriptor(it) } }
            .mapLatest { descriptors ->
                val mapping = PaymentMethodDescriptorMapping(descriptors)
                // we get the descriptor we need for standalone PM
                if (config.isStandalonePaymentMethod) {
                    val paymentMethod = config.intent.paymentMethod
                    val descriptor = mapping.getDescriptorFor(paymentMethod)
                    descriptor?.let {
                        PaymentDescriptorsHolder(descriptors, descriptor)
                    } ?: throw IllegalStateException(STANDALONE_PAYMENT_METHOD_ERROR)
                } else {
                    PaymentDescriptorsHolder(descriptors)
                }
            }.flowOn(dispatcher)
            .catch {
                eventDispatcher.dispatchEvent(it.toCheckoutErrorEvent(it.message.orEmpty()))
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

    private companion object {

        const val STANDALONE_PAYMENT_METHOD_ERROR =
            "Failed to initialise due to missing configuration. Please ensure the " +
                "requested payment method has been configured in Primer's dashboard."
    }

    data class PaymentDescriptorsHolder(
        val descriptors: List<PaymentMethodDescriptor>,
        val selectedPaymentMethodDescriptor: PaymentMethodDescriptor? = null,
    )
}
