package io.primer.android.payment.gocardless

import android.content.Context
import android.view.View
import io.primer.android.payment.GOCARDLESS_IDENTIFIER
import io.primer.android.PaymentMethod
import io.primer.android.R
import io.primer.android.UniversalCheckout
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.payment.*
import io.primer.android.viewmodel.PrimerViewModel
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
internal class GoCardless(
    viewModel: PrimerViewModel,
    config: PaymentMethodRemoteConfig,
    val options: PaymentMethod.GoCardless,
) : PaymentMethodDescriptor(viewModel, config), DIAppComponent {

    private val checkoutConfig: CheckoutConfig by inject()

    override val identifier: String
        get() = GOCARDLESS_IDENTIFIER

    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = NewFragmentBehaviour(
            GoCardlessViewFragment::newInstance,
            returnToPreviousOnBack = !checkoutConfig.standalone
        )

    override val type: PaymentMethodType
        get() = PaymentMethodType.FORM

    override val vaultCapability: VaultCapability
        get() = VaultCapability.VAULT_ONLY

    override fun createButton(context: Context): View {
        return View.inflate(context, R.layout.payment_method_button_direct_debit, null)
    }
}
