package io.primer.android.payment.async.dotpay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import io.primer.android.R
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.fragments.bank.DotPayBankSelectionFragment

internal class AdyenDotpayPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodRemoteConfig,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    override val title = "DOTPAY"

    override val selectedBehaviour =
        NewFragmentBehaviour(
            DotPayBankSelectionFragment::newInstance, returnToPreviousOnBack = true
        )

    override val behaviours: List<SelectedPaymentMethodBehaviour>
        get() = listOf(AsyncPaymentMethodBehaviour(this))

    override val type: PaymentMethodUiType = PaymentMethodUiType.FORM

    override fun createButton(container: ViewGroup): View {
        val button = LayoutInflater.from(container.context).inflate(
            R.layout.payment_method_button_dotpay,
            container,
            false
        )

        val icon = button.findViewById<ImageView>(R.id.icon)
        icon.setImageResource(
            if (localConfig.theme.isDarkMode == true) R.drawable.ic_logo_dotpay_dark
            else R.drawable.ic_logo_dotpay_light
        )
        return button
    }
}
