package io.primer.android.payment.gocardless

import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.di.DIAppComponent
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
internal class GoCardlessDescriptor(
    config: PaymentMethodConfigDataResponse,
    val options: GoCardless,
) : PaymentMethodDescriptor(config), DIAppComponent {

    private val localConfig: PrimerConfig by inject()
    private val theme: PrimerTheme by inject()

    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = NewFragmentBehaviour(
            GoCardlessViewFragment::newInstance,
            returnToPreviousOnBack = !localConfig.isStandalonePaymentMethod
        )

    override val type: PaymentMethodUiType
        get() = PaymentMethodUiType.FORM

    override val vaultCapability: VaultCapability
        get() = VaultCapability.VAULT_ONLY
}
