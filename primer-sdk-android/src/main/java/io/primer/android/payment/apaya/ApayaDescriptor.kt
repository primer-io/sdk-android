package io.primer.android.payment.apaya

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import io.primer.android.PrimerTheme
import io.primer.android.R
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.payment.APAYA_IDENTIFIER
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
internal class ApayaDescriptor constructor(
    val localConfig: PrimerConfig,
    val options: Apaya,
    config: PaymentMethodRemoteConfig,
) : PaymentMethodDescriptor(config), DIAppComponent {

    companion object {

        const val APAYA_REQUEST_CODE = 1001
    }
    private val theme: PrimerTheme by inject()

    override val identifier: String = APAYA_IDENTIFIER

    override val selectedBehaviour: SelectedPaymentMethodBehaviour = RecurringApayaBehaviour(this)

    override val type: PaymentMethodType
        get() = PaymentMethodType.SIMPLE_BUTTON

    override val vaultCapability: VaultCapability
        get() = VaultCapability.VAULT_ONLY

    override fun createButton(container: ViewGroup): View {
        val button = LayoutInflater.from(container.context).inflate(
            R.layout.payment_method_button_pay_mobile,
            container,
            false
        )
        val text = button.findViewById<TextView>(R.id.pay_mobile_preview_button)
        val drawable = ContextCompat.getDrawable(
            container.context,
            R.drawable.ic_mobile
        )

        text.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)

        text.setTextColor(theme.paymentMethodButton.text.defaultColor.getColor(container.context))

        val icon = text.compoundDrawables

        DrawableCompat.setTint(
            DrawableCompat.wrap(icon[0]),
            theme.paymentMethodButton.text.defaultColor.getColor(container.context)
        )

        return button
    }
}
