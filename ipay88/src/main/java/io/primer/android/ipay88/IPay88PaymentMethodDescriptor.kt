package io.primer.android.ipay88

import com.ipay.IPayIH
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.paymentmethods.HeadlessDefinition
import io.primer.android.paymentmethods.PaymentMethodDescriptor
import io.primer.android.paymentmethods.VaultCapability

internal class IPay88PaymentMethodDescriptor(
    val options: IPay88PaymentMethod,
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse
) : PaymentMethodDescriptor(config, localConfig) {

    internal val paymentMethod = IPayIH.PAY_METHOD_CREDIT_CARD

    override val vaultCapability: VaultCapability = VaultCapability.SINGLE_USE_ONLY

    override val headlessDefinition: HeadlessDefinition
        get() = HeadlessDefinition(
            listOf(
                PrimerPaymentMethodManagerCategory.NATIVE_UI
            )
        )
}
