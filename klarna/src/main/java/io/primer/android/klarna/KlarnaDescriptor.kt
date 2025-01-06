package io.primer.android.klarna

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.paymentmethods.HeadlessDefinition
import io.primer.android.paymentmethods.PaymentMethodDescriptor
import io.primer.android.paymentmethods.VaultCapability
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal open class KlarnaDescriptor(
    val options: Klarna,
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse,
) : PaymentMethodDescriptor(config, localConfig) {
    override val vaultCapability = VaultCapability.SINGLE_USE_AND_VAULT

    override val headlessDefinition: HeadlessDefinition?
        get() = HeadlessDefinition(listOf(PrimerPaymentMethodManagerCategory.KLARNA))
}
