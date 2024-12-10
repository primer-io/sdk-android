package io.primer.android.vouchers.retailOutlets

import io.primer.android.PrimerRetailerData
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.core.di.DISdkComponent
import io.primer.android.paymentmethods.HeadlessDefinition
import io.primer.android.paymentmethods.PaymentMethodDescriptor
import io.primer.android.paymentmethods.VaultCapability

internal class RetailOutletsDescriptor(
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse
) : PaymentMethodDescriptor(config, localConfig), DISdkComponent {

    override val vaultCapability: VaultCapability = VaultCapability.SINGLE_USE_ONLY

    override val headlessDefinition: HeadlessDefinition
        get() = HeadlessDefinition(
            paymentMethodManagerCategories = listOf(PrimerPaymentMethodManagerCategory.RAW_DATA),
            rawDataDefinition = HeadlessDefinition.RawDataDefinition(PrimerRetailerData::class)
        )
}
