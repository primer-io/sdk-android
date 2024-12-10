package io.primer.android.card

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.core.di.DISdkComponent
import io.primer.android.paymentmethods.HeadlessDefinition
import io.primer.android.paymentmethods.PaymentMethodDescriptor
import io.primer.android.paymentmethods.VaultCapability
import io.primer.android.components.domain.core.models.card.PrimerCardData

internal class CreditCard(
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse
) : PaymentMethodDescriptor(config, localConfig), DISdkComponent {

    override val vaultCapability: VaultCapability = VaultCapability.SINGLE_USE_AND_VAULT

    override val headlessDefinition: HeadlessDefinition
        get() = HeadlessDefinition(
            paymentMethodManagerCategories = listOf(PrimerPaymentMethodManagerCategory.RAW_DATA),
            rawDataDefinition = HeadlessDefinition.RawDataDefinition(PrimerCardData::class)
        )
}
