package io.primer.android.payment.gocardless

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.primer.android.PrimerTheme
import io.primer.android.R
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
internal class GoCardlessDescriptor(
    config: PaymentMethodRemoteConfig,
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

    override fun createButton(container: ViewGroup): View {
        val button = LayoutInflater.from(container.context).inflate(
            R.layout.payment_method_button_direct_debit,
            container,
            false
        )
        val text = button.findViewById<TextView>(R.id.direct_debit_button_text)
        text.setTextColor(theme.paymentMethodButton.text.defaultColor.getColor(container.context))

        return button
    }
}
