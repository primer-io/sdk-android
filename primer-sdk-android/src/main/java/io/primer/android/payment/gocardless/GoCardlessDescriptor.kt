package io.primer.android.payment.gocardless

import android.content.Context
import android.view.View
import io.primer.android.payment.GOCARDLESS_IDENTIFIER
import io.primer.android.R
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
internal class GoCardlessDescriptor(
    config: PaymentMethodRemoteConfig,
    val options: GoCardless,
) : PaymentMethodDescriptor(config), DIAppComponent {

    private val checkoutConfig: CheckoutConfig by inject()

    override val identifier: String
        get() = GOCARDLESS_IDENTIFIER

    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = NewFragmentBehaviour(
            GoCardlessViewFragment::newInstance,
            returnToPreviousOnBack = !checkoutConfig.isStandalonePaymentMethod
        )

    override val type: PaymentMethodType
        get() = PaymentMethodType.FORM

    override val vaultCapability: VaultCapability
        get() = VaultCapability.VAULT_ONLY

    override fun createButton(context: Context): View {
        return View.inflate(context, R.layout.payment_method_button_direct_debit, null)
    }
}
