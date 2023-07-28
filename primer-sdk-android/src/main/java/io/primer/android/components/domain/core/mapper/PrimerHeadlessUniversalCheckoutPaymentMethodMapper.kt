package io.primer.android.components.domain.core.mapper

import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import io.primer.android.domain.payments.methods.repository.PaymentMethodDescriptorsRepository
import io.primer.android.payment.VaultCapability

internal class PrimerHeadlessUniversalCheckoutPaymentMethodMapper(
    private val paymentMethodDescriptorsRepository: PaymentMethodDescriptorsRepository
) {

    fun getPrimerHeadlessUniversalCheckoutPaymentMethod(
        paymentMethodType: String
    ): PrimerHeadlessUniversalCheckoutPaymentMethod {
        return paymentMethodDescriptorsRepository.getPaymentMethodDescriptors()
            .firstOrNull { descriptor -> descriptor.config.type == paymentMethodType }
            ?.let { descriptor ->
                val headlessDefinition =
                    descriptor.headlessDefinition ?: throw IllegalStateException(
                        "Missing payment method manager descriptor for $paymentMethodType"
                    )

                PrimerHeadlessUniversalCheckoutPaymentMethod(
                    descriptor.config.type,
                    descriptor.vaultCapability.toPrimerSupportedIntents(),
                    headlessDefinition.paymentMethodManagerCategories,
                    headlessDefinition.rawDataDefinition?.requiredInputDataClass
                )
            } ?: throw UnsupportedPaymentMethodException(paymentMethodType)
    }
}

internal fun VaultCapability.toPrimerSupportedIntents() =
    when (this) {
        VaultCapability.VAULT_ONLY -> listOf(PrimerSessionIntent.VAULT)
        VaultCapability.SINGLE_USE_ONLY -> listOf(PrimerSessionIntent.CHECKOUT)
        VaultCapability.SINGLE_USE_AND_VAULT -> listOf(
            PrimerSessionIntent.CHECKOUT,
            PrimerSessionIntent.VAULT
        )
    }
