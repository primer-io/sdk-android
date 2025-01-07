package io.primer.android.components.implementation.domain.mapper

import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.components.implementation.domain.PaymentMethodDescriptorsRepository
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import io.primer.android.paymentmethods.VaultCapability

internal class PrimerHeadlessUniversalCheckoutPaymentMethodMapper(
    private val paymentMethodDescriptorsRepository: PaymentMethodDescriptorsRepository,
) {
    fun getPrimerHeadlessUniversalCheckoutPaymentMethod(
        paymentMethodType: String,
    ): PrimerHeadlessUniversalCheckoutPaymentMethod {
        return paymentMethodDescriptorsRepository.getPaymentMethodDescriptors()
            .firstOrNull { descriptor -> descriptor.config.type == paymentMethodType }
            ?.let { descriptor ->
                val headlessDefinition =
                    requireNotNull(descriptor.headlessDefinition) {
                        "Missing payment method manager descriptor for $paymentMethodType"
                    }

                PrimerHeadlessUniversalCheckoutPaymentMethod(
                    paymentMethodType = descriptor.config.type,
                    paymentMethodName = descriptor.config.name,
                    supportedPrimerSessionIntents = descriptor.vaultCapability.toPrimerSupportedIntents(),
                    paymentMethodManagerCategories = headlessDefinition.paymentMethodManagerCategories,
                    requiredInputDataClass = headlessDefinition.rawDataDefinition?.requiredInputDataClass,
                )
            } ?: throw UnsupportedPaymentMethodException(paymentMethodType)
    }
}

internal fun VaultCapability.toPrimerSupportedIntents() =
    when (this) {
        VaultCapability.VAULT_ONLY -> listOf(PrimerSessionIntent.VAULT)
        VaultCapability.SINGLE_USE_ONLY -> listOf(PrimerSessionIntent.CHECKOUT)
        VaultCapability.SINGLE_USE_AND_VAULT ->
            listOf(
                PrimerSessionIntent.CHECKOUT,
                PrimerSessionIntent.VAULT,
            )
    }
