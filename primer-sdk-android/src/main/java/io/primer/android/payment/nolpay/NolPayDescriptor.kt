package io.primer.android.payment.nolpay

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.HeadlessDefinition
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability

internal class NolPayDescriptor constructor(
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse
) : PaymentMethodDescriptor(config, localConfig) {

    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = TODO("Not yet implemented")
    override val type: PaymentMethodUiType = PaymentMethodUiType.FORM
    override val vaultCapability: VaultCapability = VaultCapability.SINGLE_USE_AND_VAULT
    override val headlessDefinition: HeadlessDefinition = HeadlessDefinition(
        listOf(
            PrimerPaymentMethodManagerCategory.NOL_PAY
        )
    )
}
