package io.primer.android.payment.apaya

import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.di.DIAppComponent
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import io.primer.android.ui.payment.LoadingState

internal class ApayaDescriptor constructor(
    val localConfig: PrimerConfig,
    val options: Apaya,
    config: PaymentMethodConfigDataResponse,
) : PaymentMethodDescriptor(config), DIAppComponent {

    companion object {

        const val APAYA_REQUEST_CODE = 1001
    }

    override val behaviours: List<SelectedPaymentMethodBehaviour>
        get() = if (localConfig.settings.uiOptions.isInitScreenEnabled.not() &&
            localConfig.isStandalonePaymentMethod
        ) listOf() else super.behaviours

    override val selectedBehaviour: SelectedPaymentMethodBehaviour = RecurringApayaBehaviour(this)

    override val type: PaymentMethodUiType
        get() = PaymentMethodUiType.SIMPLE_BUTTON

    override val vaultCapability: VaultCapability
        get() = VaultCapability.VAULT_ONLY

    override fun getLoadingState() = LoadingState(R.drawable.ic_logo_apaya)
}
