package io.primer.android.payment.apaya

import io.primer.android.R
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.di.DISdkComponent
import io.primer.android.payment.HeadlessDefinition
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.SelectedPaymentMethodManagerBehaviour
import io.primer.android.payment.VaultCapability
import io.primer.android.ui.payment.LoadingState

internal class ApayaDescriptor constructor(
    val options: Apaya,
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse
) : PaymentMethodDescriptor(config, localConfig), DISdkComponent {

    override val selectedBehaviour: SelectedPaymentMethodBehaviour =
        SelectedPaymentMethodManagerBehaviour(options.type, localConfig.paymentMethodIntent)

    override val type: PaymentMethodUiType
        get() = PaymentMethodUiType.SIMPLE_BUTTON

    override val vaultCapability: VaultCapability
        get() = VaultCapability.VAULT_ONLY

    override val headlessDefinition: HeadlessDefinition
        get() = HeadlessDefinition(listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI))

    override fun getLoadingState() = LoadingState(R.drawable.ic_logo_apaya)
}
